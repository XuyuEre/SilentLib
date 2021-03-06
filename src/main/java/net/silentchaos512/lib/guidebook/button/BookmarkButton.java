/*
 * Inspired by the Actually Additions booklet by Ellpeck.
 */

package net.silentchaos512.lib.guidebook.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.silentchaos512.lib.guidebook.GuideBook;
import net.silentchaos512.lib.guidebook.IGuideChapter;
import net.silentchaos512.lib.guidebook.IGuidePage;
import net.silentchaos512.lib.guidebook.gui.GuiGuide;
import net.silentchaos512.lib.guidebook.gui.GuiPage;
import net.silentchaos512.lib.guidebook.misc.GuideBookUtils;
import net.silentchaos512.lib.util.AssetUtil;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BookmarkButton extends GuiButton {

    private final GuiGuide booklet;
    protected GuideBook book;
    private IGuidePage assignedPage;

    public BookmarkButton(int id, int x, int y, GuiGuide booklet) {
        super(id, x, y, 16, 16, "");
        this.booklet = booklet;
        this.book = booklet.book;
    }

    public void onPressed() {
        if (this.assignedPage != null) {
            if (GuiScreen.isShiftKeyDown()) {
                this.assignedPage = null;
            } else if (!(this.booklet instanceof GuiPage)
                    || ((GuiPage) this.booklet).pages[0] != this.assignedPage) {
                GuiPage gui = GuideBookUtils.createPageGui(book, this.booklet.previousScreen, this.booklet,
                        this.assignedPage);
                Minecraft.getMinecraft().displayGuiScreen(gui);
            }
        } else {
            if (this.booklet instanceof GuiPage) {
                this.assignedPage = ((GuiPage) this.booklet).pages[0];
            }
        }
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float par4) {
        if (this.visible) {
            minecraft.getTextureManager().bindTexture(book.getResourceGadgets());
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
                    && mouseY < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            if (k == 0) {
                k = 1;
            }

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);
            int renderHeight = 25;
            this.drawTexturedModalRect(this.x, this.y,
                    224 + (this.assignedPage == null ? 0 : 16), 14 - renderHeight + k * renderHeight,
                    this.width, renderHeight);
            this.mouseDragged(minecraft, mouseX, mouseY);

            if (this.assignedPage != null) {
                ItemStack display = this.assignedPage.getChapter().getDisplayItemStack();
                if (!display.isEmpty()) {
                    GlStateManager.pushMatrix();
                    AssetUtil.renderStackToGui(display, this.x + 2, this.y + 1, 0.725F);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public void drawHover(int mouseX, int mouseY) {
        if (this.isMouseOver()) {
            List<String> list = new ArrayList<>();

            if (this.assignedPage != null) {
                IGuideChapter chapter = this.assignedPage.getChapter();
                list.add(TextFormatting.GOLD + chapter.getLocalizedName() + ", Page " + (chapter.getPageIndex(this.assignedPage) + 1));
                list.add(book.i18n.translate("guide", "bookmarkButton.bookmark.openDesc"));
                list.add(TextFormatting.ITALIC + book.i18n.translate("guide", "bookmarkButton.bookmark.removeDesc"));
            } else {
                list.add(TextFormatting.GOLD + book.i18n.translate("guide", "bookmarkButton.noBookmark.name"));
                if (this.booklet instanceof GuiPage) {
                    list.add(book.i18n.translate("guide", "bookmarkButton.noBookmark.pageDesc"));
                } else {
                    list.add(book.i18n.translate("guide", "bookmarkButton.noBookmark.notPageDesc"));
                }
            }

            Minecraft mc = Minecraft.getMinecraft();
            GuiUtils.drawHoveringText(list, mouseX, mouseY, mc.displayWidth, mc.displayHeight, -1, mc.fontRenderer);
        }
    }
}
