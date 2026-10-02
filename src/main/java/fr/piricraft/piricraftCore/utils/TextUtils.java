package fr.piricraft.piricraftCore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TextUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component color(String text) {
        return MINI_MESSAGE.deserialize(text);
    }

}
