/*
 * Silent Lib -- ItemLootContainer
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.lib.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.silentchaos512.lib.SilentLib;
import net.silentchaos512.lib.util.PlayerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An item that gives the player items from a loot table when used, similar to a loot bag. A default
 * loot table must be specified, but ultimately an NBT tag is used to determine which loot table to
 * pull items from. This could be extended to not use loot tables (see {@link
 * #getLootDrops(ItemStack, EntityPlayerMP)}).
 *
 * @author SilentChaos512
 * @since 3.0.2
 */
public class ItemLootContainer extends Item {
    private static final String NBT_ROOT = SilentLib.MOD_ID + ".LootContainer";
    private static final String NBT_LOOT_TABLE = "LootTable";

    private final ResourceLocation defaultLootTable;

    public ItemLootContainer(ResourceLocation defaultLootTable) {
        this.defaultLootTable = defaultLootTable;
    }

    /**
     * Get a stack of this item with the default loot table.
     *
     * @return A stack with appropriate NBT tags set and stack size of one
     */
    public ItemStack getStack() {
        return getStack(this.defaultLootTable);
    }

    /**
     * Get a stack of this item with the specified loot table.
     *
     * @param lootTable The loot table to assign to the stack
     * @return A stack with appropriate NBT tags set and stack size of one
     */
    public ItemStack getStack(ResourceLocation lootTable) {
        ItemStack result = new ItemStack(this);
        result.getOrCreateSubCompound(NBT_ROOT).setString(NBT_LOOT_TABLE, lootTable.toString());
        return result;
    }

    /**
     * Get the items to give the player when used. By default, this uses the loot table specified in
     * the NBT of {@code heldItem}. Can be overridden for different behavior. This implementation is
     * similar to {@link net.minecraft.advancements.AdvancementRewards#apply(EntityPlayerMP)}.
     *
     * @param heldItem The loot container item being used
     * @param player   The player using the item
     * @return A collection of items to give to the player
     */
    protected Collection<ItemStack> getLootDrops(ItemStack heldItem, EntityPlayerMP player) {
        ResourceLocation lootTable = getLootTable(heldItem);
        LootContext lootContext = (new LootContext.Builder(player.getServerWorld())).withLootedEntity(player)
                .withPlayer(player).withLuck(player.getLuck()).build();
        return new ArrayList<>(player.world.getLootTableManager().getLootTableFromLocation(lootTable)
                .generateLootForPools(player.getRNG(), lootContext));
    }

    private ResourceLocation getLootTable(ItemStack stack) {
        NBTTagCompound tags = stack.getOrCreateSubCompound(NBT_ROOT);
        if (tags.hasKey(NBT_LOOT_TABLE)) {
            // TODO: Does the string need to be validated? (I think I heard 1.13 will throw exceptions...)
            return new ResourceLocation(tags.getString(NBT_LOOT_TABLE));
        }
        return this.defaultLootTable;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (flagIn.isAdvanced()) {
            tooltip.add(TextFormatting.DARK_GRAY + "Loot Table: " + this.getLootTable(stack));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack heldItem = playerIn.getHeldItem(handIn);
        if (!(playerIn instanceof EntityPlayerMP))
            return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
        EntityPlayerMP playerMP = (EntityPlayerMP) playerIn;

        // Generate items from loot table, give to player.
        Collection<ItemStack> lootDrops = this.getLootDrops(heldItem, playerMP);
        if (lootDrops.isEmpty())
            SilentLib.LOGGER.warn("ItemLootContainer has no drops? {}", heldItem);
        lootDrops.forEach(stack -> PlayerHelper.giveItem(playerMP, stack));

        // Play item pickup sound...
        playerMP.world.playSound(null, playerMP.posX, playerMP.posY, playerMP.posZ,
                SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                ((playerMP.getRNG().nextFloat() - playerMP.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
        heldItem.shrink(1);
        return ActionResult.newResult(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Don't care if clicked on a block or empty space
        this.onItemRightClick(worldIn, player, hand).getType();
        return EnumActionResult.FAIL;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        items.add(this.getStack());
    }
}
