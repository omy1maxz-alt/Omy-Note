package com.example.util

import android.content.Context
import com.example.data.local.NoteRepository
import com.example.data.model.NotesDataPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(NotesDataPayload::class.java)

    suspend fun createBackupZip(
        context: Context,
        noteRepository: NoteRepository,
        outputStream: OutputStream
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val jsonContent = noteRepository.getNotesJsonString()
            val mediaDir = noteRepository.mediaDir

            var itemsCount = 0

            ZipOutputStream(BufferedOutputStream(outputStream)).use { zos ->
                // 1. Write notes.json
                val jsonEntry = ZipEntry("notes.json")
                zos.putNextEntry(jsonEntry)
                zos.write(jsonContent.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
                itemsCount++

                // 2. Write media files
                if (mediaDir.exists() && mediaDir.isDirectory) {
                    val mediaFiles = mediaDir.listFiles() ?: emptyArray()
                    val buffer = ByteArray(8192)
                    for (file in mediaFiles) {
                        if (file.isFile) {
                            val mediaEntry = ZipEntry("media/${file.name}")
                            zos.putNextEntry(mediaEntry)
                            FileInputStream(file).use { fis ->
                                var len: Int
                                while (fis.read(buffer).also { len = it } > 0) {
                                    zos.write(buffer, 0, len)
                                }
                            }
                            zos.closeEntry()
                            itemsCount++
                        }
                    }
                }
                zos.finish()
            }
            Result.success(itemsCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackupZip(
        context: Context,
        noteRepository: NoteRepository,
        inputStream: InputStream,
        isReplace: Boolean
    ): Result<Int> = withContext(Dispatchers.IO) {
        val tempRestoreDir = File(context.cacheDir, "temp_restore_${System.currentTimeMillis()}").apply { mkdirs() }
        try {
            var extractedJson: String? = null
            val extractedMedia = mutableListOf<File>()

            // 1. Extract and sanitize entries
            ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                val buffer = ByteArray(8192)

                while (entry != null) {
                    val name = entry.name

                    // Directory traversal prevention
                    if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                        throw SecurityException("Potential path traversal exploit detected in backup archive: $name")
                    }

                    if (!entry.isDirectory) {
                        val outputFile = File(tempRestoreDir, name)
                        outputFile.parentFile?.mkdirs()

                        FileOutputStream(outputFile).use { fos ->
                            var len: Int
                            while (zis.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }

                        if (name == "notes.json") {
                            extractedJson = outputFile.readText(Charsets.UTF_8)
                        } else if (name.startsWith("media/")) {
                            extractedMedia.add(outputFile)
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            if (extractedJson == null) {
                return@withContext Result.failure(IllegalArgumentException("Invalid backup file: notes.json missing."))
            }

            // 2. Validate JSON structure
            val payload = adapter.fromJson(extractedJson)
                ?: return@withContext Result.failure(IllegalArgumentException("Corrupted notes.json inside backup."))

            // 3. Copy extracted media to app media directory
            val targetMediaDir = noteRepository.mediaDir
            for (mediaFile in extractedMedia) {
                val destFile = File(targetMediaDir, mediaFile.name)
                mediaFile.copyTo(destFile, overwrite = true)
            }

            // 4. Update repository
            if (isReplace) {
                noteRepository.replaceData(payload.notes, payload.categories)
            } else {
                noteRepository.mergeData(payload.notes, payload.categories)
            }

            Result.success(payload.notes.size)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // Cleanup temp dir
            tempRestoreDir.deleteRecursively()
        }
    }
}
