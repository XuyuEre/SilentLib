/*
 * SilentLib - SilentLibCommonEvents
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

package net.silentchaos512.lib.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.silentchaos512.lib.SilentLib;
import net.silentchaos512.lib.item.ItemGuideBookSL;
import net.silentchaos512.lib.util.EntityHelper;
import net.silentchaos512.lib.util.PlayerHelper;

/**
 * Silent Lib's common event handler. Do not call any functions of this class.
 *
 * @author SilentChaos512
 * @since 2.1.4
 */
public final class SilentLibCommonEvents {

  private static final String NBT_ROOT_GUIDE_BOOKS = "silentlib_guide_books";

  /**
   * Called when a player logs in. Used to give guide books to the player.
   *
   * @param event
   */
  @SubscribeEvent
  public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

    EntityPlayer player = event.player;
    NBTTagCompound forgeData = player.getEntityData();
    if (!forgeData.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
      forgeData.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());

    NBTTagCompound persistedData = forgeData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    if (!persistedData.hasKey(NBT_ROOT_GUIDE_BOOKS))
      persistedData.setTag(NBT_ROOT_GUIDE_BOOKS, new NBTTagCompound());

    NBTTagCompound guideData = persistedData.getCompoundTag(NBT_ROOT_GUIDE_BOOKS);

    int id = 0;
    ItemGuideBookSL item = ItemGuideBookSL.getBookById(id);
    while (item != null && item.giveBookOnFirstLogin) {
      if (!guideData.getBoolean(item.getFullName())) {
        guideData.setBoolean(item.getFullName(), true);
        PlayerHelper.giveItem(player, new ItemStack(item));
        SilentLib.logHelper.info("Player has been given guide book " + item.getFullName());
      }

      item = ItemGuideBookSL.getBookById(++id);
    }
  }

  @SubscribeEvent
  public void onWorldTick(WorldTickEvent event) {

    if (event.phase == Phase.START) {
      // World tick pre.
      EntityHelper.handleSpawns();
    }
  }
}