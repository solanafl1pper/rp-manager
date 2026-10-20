package com.solana.rpmanager.util;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public final class Texts {
    private Texts() {}

    public static MutableText literal(String text) {
        return new LiteralText(text != null ? text : "");
    }
}
