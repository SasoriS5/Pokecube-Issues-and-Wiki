package pokecube.core.client.gui.watch;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.TeleportsPage.TeleOption;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.PokecubeSerializer.TeleDest;

public class TeleportsPage extends ListPage<TeleOption>
{
    public static class TeleOption extends AbstractList.AbstractListEntry<TeleOption>
    {
        final TeleportsPage   parent;
        final int             offsetY;
        final Minecraft       mc;
        final TeleDest        dest;
        final TextFieldWidget text;
        final Button          delete;
        final Button          confirm;
        final Button          moveUp;
        final Button          moveDown;
        final int             guiHeight;

        public TeleOption(final Minecraft mc, final int offsetY, final TeleDest dest, final TextFieldWidget text,
                final int height, final TeleportsPage parent)
        {
            this.dest = dest;
            this.text = text;
            this.mc = mc;
            this.offsetY = offsetY;
            this.guiHeight = height;
            this.parent = parent;
            this.confirm = new Button(0, 0, 10, 10, "Y", b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                // Send packet for removal server side
                PacketPokedex.sendRemoveTelePacket(this.dest.index);
                // Also remove it client side so we update now.
                TeleportHandler.unsetTeleport(this.dest.index, this.parent.watch.player.getCachedUniqueIdString());
                // Update the list for the page.
                this.parent.initList();
            });
            this.delete = new Button(0, 0, 10, 10, "x", b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.confirm.active = !this.confirm.active;
            });
            this.delete.setFGColor(0xFFFF0000);
            this.confirm.active = false;
            this.moveUp = new Button(0, 0, 10, 10, "\u21e7", b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index - 1);
                // Update the list for the page.
                this.parent.initList();
            });
            this.moveDown = new Button(0, 0, 10, 10, "\u21e9", b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index + 1);
                // Update the list for the page.
                this.parent.initList();
            });
            this.moveUp.active = dest.index != 0;
            this.moveDown.active = dest.index != parent.locations.size() - 1;
        }

        @Override
        public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_)
        {
            if (this.text.isFocused())
            {
                if (keyCode == GLFW.GLFW_KEY_ENTER)
                {
                    if (!text.getText().equals(dest.getName()))
                    {
                        PacketPokedex.sendRenameTelePacket(text.getText(), dest.index);
                        dest.setName(text.getText());
                        return true;
                    }
                    return false;
                }
                return this.text.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
            }
            return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
        }

        @Override
        public boolean charTyped(final char typedChar, final int keyCode)
        {
            if (this.text.isFocused()) { return this.text.charTyped(typedChar, keyCode); }

            if (keyCode == GLFW.GLFW_KEY_ENTER)
            {
                if (!this.text.getText().equals(this.dest.getName()))
                {
                    PacketPokedex.sendRenameTelePacket(this.text.getText(), this.dest.index);
                    this.dest.setName(this.text.getText());
                    return true;
                }
            }
            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseEvent)
        {
            boolean fits = true;
            fits = this.text.y >= this.offsetY;
            fits = fits && mouseX - this.text.x >= 0;
            fits = fits && mouseX - this.text.x <= this.text.getWidth();
            fits = fits && this.text.y + this.text.getHeight() <= this.offsetY + this.guiHeight;
            this.text.setFocused(fits);
            if (this.delete.isMouseOver(mouseX, mouseY))
            {
                delete.playDownSound(this.mc.getSoundHandler());
                confirm.active = !confirm.active;
            }
            else if (this.confirm.isMouseOver(mouseX, mouseY) && this.confirm.active)
            {
                confirm.playDownSound(this.mc.getSoundHandler());
                // Send packet for removal server side
                PacketPokedex.sendRemoveTelePacket(dest.index);
                // Also remove it client side so we update now.
                TeleportHandler.unsetTeleport(dest.index, parent.watch.player.getCachedUniqueIdString());
                // Update the list for the page.
                parent.initList();
            }
            else if (this.moveUp.isMouseOver(mouseX, mouseY) && this.moveUp.active)
            {
                moveUp.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(dest.index, dest.index - 1);
                // Update the list for the page.
                parent.initList();
            }
            else if (this.moveDown.isMouseOver(mouseX, mouseY) && this.moveDown.active)
            {
                moveDown.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(dest.index, dest.index + 1);
                // Update the list for the page.
                parent.initList();
            }
            return fits;
        }

        @Override
        public void render(final int slotIndex, final int y, final int x, final int listWidth, final int slotHeight,
                final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
        {
            boolean fits = true;
            this.text.x = x - 2;
            this.text.y = y - 4;
            this.delete.y = y - 5;
            this.delete.x = x - 1 + this.text.getWidth();
            this.confirm.y = y - 5;
            this.confirm.x = x - 2 + 10 + this.text.getWidth();
            this.moveUp.y = y - 5;
            this.moveUp.x = x - 2 + 18 + this.text.getWidth();
            this.moveDown.y = y - 5;
            this.moveDown.x = x - 2 + 26 + this.text.getWidth();
            fits = this.text.y >= this.offsetY;
            fits = fits && this.text.y + this.text.getHeight() <= this.offsetY + this.guiHeight;
            if (fits)
            {
                this.text.render(mouseX, mouseY, partialTicks);
                this.delete.render(mouseX, mouseY, partialTicks);
                this.confirm.render(mouseX, mouseY, partialTicks);
                this.moveUp.render(mouseX, mouseY, partialTicks);
                this.moveDown.render(mouseX, mouseY, partialTicks);
            }
        }
    }

    protected List<TeleDest>        locations;
    protected List<TextFieldWidget> teleNames = Lists.newArrayList();

    public TeleportsPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.teleports"), watch);
    }

    @Override
    public void initList()
    {
        super.initList();
        this.locations = TeleportHandler.getTeleports(this.watch.player.getCachedUniqueIdString());
        this.teleNames.clear();
        final int offsetX = (this.watch.width - 160) / 2 + 10;
        final int offsetY = (this.watch.height - 160) / 2 + 24;
        final int height = 120;
        final int width = 146;
        this.list = new ScrollGui<>(this, this.minecraft, width, height, 10, offsetX, offsetY);
        for (final TeleDest d : this.locations)
        {
            final TextFieldWidget name = new TextFieldWidget(this.font, 0, 0, 104, 10, "");
            this.teleNames.add(name);
            name.setText(d.getName());
            this.list.addEntry(new TeleOption(this.minecraft, offsetY, d, name, height, this));
        }
        this.children.add(this.list);
    }
}
