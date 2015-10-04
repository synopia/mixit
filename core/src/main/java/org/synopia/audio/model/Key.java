package org.synopia.audio.model;

/**
 * Created by synopia on 25.09.2015.
 */
public enum Key {
    A_MAJOR("11B"),
    A_MINOR("8A"),
    B_FLAT_MAJOR("6B"),
    B_FLAT_MINOR("3A"),
    B_MAJOR("1B"),
    B_MINOR("10A"),
    C_MAJOR("8B"),
    C_MINOR("5A"),
    D_FLAT_MAJOR("3B"),
    D_FLAT_MINOR("12A"),
    D_MAJOR("10B"),
    D_MINOR("7A"),
    E_FLAT_MAJOR("5B"),
    E_FLAT_MINOR("2A"),
    E_MAJOR("12B"),
    E_MINOR("9A"),
    F_MAJOR("7B"),
    F_MINOR("4A"),
    G_FLAT_MAJO("2B"),
    G_FLAT_MINOR("11A"),
    G_MAJOR("9B"),
    G_MINOR("6A"),
    A_FLAT_MAJOR("4B"),
    A_FLAT_MINOR("1A"),
    SILENCE(" ");

    private String camelot;

    Key(String camelot) {
        this.camelot = camelot;
    }

    public String getCamelot() {
        return camelot;
    }

    @Override
    public String toString() {
        return camelot;
    }

    public static Key fromCamelot(String key) {
        for (Key k : values()) {
            if (k.toString().equals(key)) {
                return k;
            }
        }
        return SILENCE;
    }
}
