package org.pocketworkstation.pckeyboard;

public class HangulAutomata {

    public static final int STATE_EMPTY = 0;
    public static final int STATE_CHO = 1;
    public static final int STATE_CHO_JUNG = 2;
    public static final int STATE_CHO_JUNG_JONG = 3;
    public static final int STATE_JUNG = 4;
    public static final int STATE_CHO_CHO = 5;

    public static final int HANGUL_BASE = 0xAC00;

    public static final int[] COMP_TO_CHO = {
        0, 1, -1, 2, -1, -1, 3, 4, 5, -1, -1, -1, -1, -1, -1, -1, 6, 7, 8, -1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
    };

    public static final int[] COMP_TO_JUNG = {
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
    };

    public static final int[] COMP_TO_JONG = {
        1, 2, 3, 4, 5, 6, 7, -1, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, -1, 18, 19, 20, 21, 22, -1, 23, 24, 25, 26, 27
    };

    public static final int[] JONG_TO_COMP = {
        0, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137,
        0x3139, 0x313a, 0x313b, 0x313c, 0x313d, 0x313e, 0x313f, 0x3140,
        0x3141, 0x3142, 0x3144, 0x3145, 0x3146, 0x3147, 0x3148,
        0x314a, 0x314b, 0x314c, 0x314d, 0x314e
    };

    private int cho = -1;
    private int jung = -1;
    private int jong = -1;
    private int state = STATE_EMPTY;

    public HangulAutomata() {
        reset();
    }

    public void reset() {
        cho = -1;
        jung = -1;
        jong = -1;
        state = STATE_EMPTY;
    }


    public boolean seed(char c) {
        if (c >= 0x3131 && c <= 0x314E) {
            // It's a single Cho/Jong
            cho = getChoIndex(c);
            if (cho != -1) {
                state = STATE_CHO;
                return true;
            }
        } else if (c >= 0x314F && c <= 0x3163) {
            // It's a single Jung
            jung = getJungIndex(c);
            if (jung != -1) {
                state = STATE_JUNG;
                return true;
            }
        } else if (c >= 0xAC00 && c <= 0xD7A3) {
            // It's a Syllable
            int code = c - 0xAC00;
            jong = code % 28;
            jung = ((code - jong) / 28) % 21;
            cho = (((code - jong) / 28) - jung) / 21;

            if (jong > 0) {
                state = STATE_CHO_JUNG_JONG;
            } else {
                jong = -1;
                state = STATE_CHO_JUNG;
            }
            return true;
        }
        return false;
    }

    public boolean isComposing() {
        return state != STATE_EMPTY;
    }

    public String getComposedString() {
        if (state == STATE_EMPTY) return "";
        if (state == STATE_CHO || state == STATE_CHO_CHO) return Character.toString((char)(0x3131 + choToCompIndex(cho)));
        if (state == STATE_JUNG) return Character.toString((char)(0x314F + jung));
        return Character.toString((char)(HANGUL_BASE + (cho * 21 * 28) + (jung * 28) + (jong == -1 ? 0 : jong)));
    }

    private int choToCompIndex(int cho) {
        for (int i = 0; i < COMP_TO_CHO.length; i++) {
            if (COMP_TO_CHO[i] == cho) return i;
        }
        return -1;
    }

    public boolean isCho(int code) {
        return code >= 0x3131 && code <= 0x314E;
    }

    public boolean isJung(int code) {
        return code >= 0x314F && code <= 0x3163;
    }

    private int getChoIndex(int code) {
        if (!isCho(code)) return -1;
        return COMP_TO_CHO[code - 0x3131];
    }

    private int getJungIndex(int code) {
        if (!isJung(code)) return -1;
        return COMP_TO_JUNG[code - 0x314F];
    }

    private int getJongIndex(int code) {
        if (!isCho(code)) return -1;
        return COMP_TO_JONG[code - 0x3131];
    }

    private int combineCho(int c1, int c2) {
        if (c1 == 0 && c2 == 0) return 1; // ㄱ + ㄱ = ㄲ
        if (c1 == 3 && c2 == 3) return 4; // ㄷ + ㄷ = ㄸ
        if (c1 == 7 && c2 == 7) return 8; // ㅂ + ㅂ = ㅃ
        if (c1 == 9 && c2 == 9) return 10; // ㅅ + ㅅ = ㅆ
        if (c1 == 12 && c2 == 12) return 13; // ㅈ + ㅈ = ㅉ
        return -1;
    }

    private int combineJung(int j1, int j2) {
        if (j1 == 8 && j2 == 0) return 9;
        if (j1 == 8 && j2 == 1) return 10;
        if (j1 == 8 && j2 == 20) return 11;
        if (j1 == 13 && j2 == 4) return 14;
        if (j1 == 13 && j2 == 5) return 15;
        if (j1 == 13 && j2 == 20) return 16;
        if (j1 == 18 && j2 == 20) return 19;
        return -1;
    }

    private int combineJong(int j1, int j2) {
        if (j1 == 1 && j2 == 19) return 3;
        if (j1 == 4 && j2 == 22) return 5;
        if (j1 == 4 && j2 == 27) return 6;
        if (j1 == 8 && j2 == 1) return 9;
        if (j1 == 8 && j2 == 16) return 10;
        if (j1 == 8 && j2 == 17) return 11;
        if (j1 == 8 && j2 == 19) return 12;
        if (j1 == 8 && j2 == 25) return 13;
        if (j1 == 8 && j2 == 26) return 14;
        if (j1 == 8 && j2 == 27) return 15;
        if (j1 == 17 && j2 == 19) return 18;
        return -1;
    }

    private int splitJong1(int jong) {
        switch(jong) {
            case 3: return 1;
            case 5: return 4;
            case 6: return 4;
            case 9: return 8;
            case 10: return 8;
            case 11: return 8;
            case 12: return 8;
            case 13: return 8;
            case 14: return 8;
            case 15: return 8;
            case 18: return 17;
        }
        return jong;
    }

    private int splitJong2(int jong) {
        switch(jong) {
            case 3: return 19;
            case 5: return 22;
            case 6: return 27;
            case 9: return 1;
            case 10: return 16;
            case 11: return 17;
            case 12: return 19;
            case 13: return 25;
            case 14: return 26;
            case 15: return 27;
            case 18: return 19;
        }
        return -1;
    }

    private int splitJung1(int jung) {
        switch(jung) {
            case 9: return 8;
            case 10: return 8;
            case 11: return 8;
            case 14: return 13;
            case 15: return 13;
            case 16: return 13;
            case 19: return 18;
        }
        return jung;
    }

    public static class ProcessResult {
        public String commitText = null;
        public String composingText = "";
    }

    public ProcessResult process(int code) {
        ProcessResult result = new ProcessResult();

        if (code == 8 || code == -5) { // Backspace
            return handleBackspace();
        }

        if (!isCho(code) && !isJung(code)) {
            result.commitText = getComposedString() + Character.toString((char)code);
            reset();
            return result;
        }

        int cCho = getChoIndex(code);
        int cJung = getJungIndex(code);
        int cJong = getJongIndex(code);

        switch (state) {
            case STATE_EMPTY:
                if (cCho != -1) {
                    cho = cCho;
                    state = STATE_CHO;
                } else if (cJung != -1) {
                    jung = cJung;
                    state = STATE_JUNG;
                }
                break;
            case STATE_CHO:
                if (cJung != -1) {
                    jung = cJung;
                    state = STATE_CHO_JUNG;
                } else if (cCho != -1) {
                    int combined = combineCho(cho, cCho);
                    if (combined != -1) {
                        cho = combined;
                    } else {
                        result.commitText = getComposedString();
                        cho = cCho;
                    }
                    state = STATE_CHO;
                }
                break;
            case STATE_JUNG:
                if (cJung != -1) {
                    int combined = combineJung(jung, cJung);
                    if (combined != -1) {
                        jung = combined;
                    } else {
                        result.commitText = getComposedString();
                        jung = cJung;
                        state = STATE_JUNG;
                    }
                } else if (cCho != -1) {
                    result.commitText = getComposedString();
                    cho = cCho;
                    state = STATE_CHO;
                }
                break;
            case STATE_CHO_JUNG:
                if (cJong != -1) {
                    jong = cJong;
                    state = STATE_CHO_JUNG_JONG;
                } else if (cJung != -1) {
                    int combined = combineJung(jung, cJung);
                    if (combined != -1) {
                        jung = combined;
                    } else {
                        result.commitText = getComposedString();
                        cho = -1;
                        jung = cJung;
                        state = STATE_JUNG;
                    }
                } else {
                    result.commitText = getComposedString();
                    cho = cCho;
                    jung = -1;
                    state = STATE_CHO;
                }
                break;
            case STATE_CHO_JUNG_JONG:
                if (cJong != -1) {
                    int combined = combineJong(jong, cJong);
                    if (combined != -1) {
                        jong = combined;
                    } else {
                        result.commitText = getComposedString();
                        cho = cCho;
                        jung = -1;
                        jong = -1;
                        state = STATE_CHO;
                    }
                } else if (cJung != -1) {
                    int splitJong = splitJong2(jong);
                    if (splitJong != -1) {
                        String oldSyllable = Character.toString((char)(HANGUL_BASE + (cho * 21 * 28) + (jung * 28) + splitJong1(jong)));
                        result.commitText = oldSyllable;
                        cho = getChoIndex(JONG_TO_COMP[splitJong]);
                        jong = -1;
                        jung = cJung;
                        state = STATE_CHO_JUNG;
                    } else {
                        String oldSyllable = Character.toString((char)(HANGUL_BASE + (cho * 21 * 28) + (jung * 28)));
                        result.commitText = oldSyllable;
                        cho = getChoIndex(JONG_TO_COMP[jong]);
                        jong = -1;
                        jung = cJung;
                        state = STATE_CHO_JUNG;
                    }
                } else if (cCho != -1) {
                    result.commitText = getComposedString();
                    cho = cCho;
                    jung = -1;
                    jong = -1;
                    state = STATE_CHO;
                }
                break;
        }

        result.composingText = getComposedString();
        return result;
    }

    public ProcessResult handleBackspace() {
        ProcessResult result = new ProcessResult();
        if (state == STATE_EMPTY) {
            return result;
        }

        switch(state) {
            case STATE_CHO:
                reset();
                break;
            case STATE_JUNG:
                int sJung = splitJung1(jung);
                if (sJung != jung) {
                    jung = sJung;
                } else {
                    reset();
                }
                break;
            case STATE_CHO_JUNG:
                int sJung2 = splitJung1(jung);
                if (sJung2 != jung) {
                    jung = sJung2;
                } else {
                    jung = -1;
                    state = STATE_CHO;
                }
                break;
            case STATE_CHO_JUNG_JONG:
                int sJong = splitJong1(jong);
                if (sJong != jong) {
                    jong = sJong;
                } else {
                    jong = -1;
                    state = STATE_CHO_JUNG;
                }
                break;
        }

        result.composingText = getComposedString();
        return result;
    }
}
