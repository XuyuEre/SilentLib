/*
 * SilentLib - LocalizationHelper
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.lib.util;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple wrapper for localization, with some helper methods to get some common key types
 * (tile.modid:..., item.modid:..., etc.)
 *
 * @author SilentChaos512
 */
@Deprecated
public class LocalizationHelper {

    /**
     * Mod ID is stored so you never need to pass it in when localizing text.
     */
    public final String modId;
    /**
     * Will replace ampersands (&) with the section sign Minecraft uses for formatting codes.
     */
    private boolean replacesAmpersandWithSectionSign = true;
    /**
     * If I18n prepends "Format error: " to the string, it will be removed if this is true.
     */
    private boolean hideFormatErrors = false;

    public LocalizationHelper(String modId) {

        this.modId = modId.toLowerCase(Locale.ROOT);
    }

    /**
     * Sets whether or not to replace ampersands with section signs. This allows formatting codes to
     * easily be used in localization files.
     */
    public LocalizationHelper setReplaceAmpersand(boolean value) {

        replacesAmpersandWithSectionSign = value;
        return this;
    }

    public LocalizationHelper setHideFormatErrors(boolean value) {

        hideFormatErrors = value;
        return this;
    }

    // ===============
    // General methods
    // ===============

    @SuppressWarnings("deprecation")
    public String getLocalizedString(String key, Object... parameters) {

        // On server, use deprecated I18n.
        if (FMLCommonHandler.instance().getSide() == Side.SERVER)
            return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, parameters);

        // On client, use the new client-side I18n.
        String str = I18n.format(key, parameters).trim();

        if (replacesAmpersandWithSectionSign)
            str = str.replaceAll("&(?=[^\\s])", "\u00a7");
        if (hideFormatErrors)
            str = str.replaceFirst("Format error: ", "");

        return str;
    }

    public String getLocalizedString(String prefix, String key, Object... parameters) {

        return getLocalizedString(prefix + "." + modId + ":" + key, parameters);
    }

    // ===============
    // Special methods
    // ===============

    public String getMiscText(String key, Object... parameters) {

        return getLocalizedString("misc", key, parameters);
    }

    public String getWikiText(String key, Object... parameters) {

        return getLocalizedString("wiki", key, parameters);
    }

    public String getBlockSubText(String blockName, String key, Object... parameters) {

        return getLocalizedString("tile", blockName + "." + key, parameters);
    }

    public String getItemSubText(String itemName, String key, Object... parameters) {

        return getLocalizedString("item", itemName + "." + key, parameters);
    }

    public String getSubText(IForgeRegistryEntry<?> object, String key, Object... parameters) {
        String prefix = getPrefixFor(object);
        ResourceLocation name = Objects.requireNonNull(object.getRegistryName());
        return getLocalizedString(prefix + "." + name.getNamespace() + "." + name.getPath() + "." + key, parameters);
    }

    // =================
    // Description lines
    // =================

    @Deprecated
    public List<String> getBlockDescriptionLines(String blockName) {
        return getDescriptionLines("tile." + modId + ":" + blockName + ".desc");
    }

    @Deprecated
    public List<String> getItemDescriptionLines(String itemName) {
        return getDescriptionLines("item." + modId + ":" + itemName + ".desc");
    }

    public List<String> getDescriptionLines(IForgeRegistryEntry<?> object) {
        String prefix = getPrefixFor(object);
        ResourceLocation name = Objects.requireNonNull(object.getRegistryName());
        return getDescriptionLines(prefix + "." + name.getNamespace() + "." + name.getPath() + ".desc");
    }

    public List<String> getDescriptionLines(String key) {
        boolean oldHideFormatErrors = hideFormatErrors;
        hideFormatErrors = true;

        List<String> list = new ArrayList<>();
        int i = 1;
        String line = getLocalizedString(key + i);
        while (!line.equals(key + i)) {
            list.add(line);
            line = getLocalizedString(key + ++i);
        }

        if (list.isEmpty()) {
            line = getLocalizedString(key);
            if (!line.equals(key)) {
                list.add(line);
            }
        }

        hideFormatErrors = oldHideFormatErrors;

        return list;
    }

    private String getPrefixFor(IForgeRegistryEntry<?> object) {
        // I assume instanceof is cheaper than the fallback below, so here are common cases...
        if (object instanceof Item)
            return "item";
        if (object instanceof Block)
            return "tile";
        return object.getClass().getName().toLowerCase(Locale.ROOT);
    }
}
