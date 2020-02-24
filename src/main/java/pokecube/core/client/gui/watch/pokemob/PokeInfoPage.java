package pokecube.core.client.gui.watch.pokemob;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob.FormeHolder;

public abstract class PokeInfoPage extends WatchPage
{
    final PokemobInfoPage parent;
    List<PokedexEntry>    entries    = Lists.newArrayList();
    List<FormeHolder>     formes     = Lists.newArrayList();
    PokedexEntry          entry      = null;
    FormeHolder           holder     = null;
    int                   entryIndex = 0;
    int                   formIndex  = 0;

    public PokeInfoPage(final PokemobInfoPage parent, final String title)
    {
        super(new TranslationTextComponent("pokewatch.title.pokeinfo." + title), parent.watch);
        this.parent = parent;
        this.entryIndex = 0;
        this.formIndex = -1;
    }

    abstract void drawInfo(int mouseX, int mouseY, float partialTicks);

    @Override
    public void init()
    {
        super.init();
        this.entry = this.parent.pokemob.getPokedexEntry();
        this.entries = Lists.newArrayList(Database.getFormes(this.entry));

        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        final String next = ">";
        final String prev = "<";
        this.addButton(new Button(x - 46, y + 4, 12, 20, next, b ->
        {
            this.entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            this.entry = Pokedex.getInstance().getNext(this.entry, i);
            this.parent.pokemob = EventsHandlerClient.getRenderMob(this.entry, this.watch.player.getEntityWorld());
            this.parent.initPages(this.parent.pokemob);
        }));
        this.addButton(new Button(x - 76, y + 4, 12, 20, prev, b ->
        {
            this.entry = this.parent.pokemob.getPokedexEntry();
            final int i = Screen.hasShiftDown() ? Screen.hasControlDown() ? 100 : 10 : 1;
            this.entry = Pokedex.getInstance().getPrevious(this.entry, i);
            this.parent.pokemob = EventsHandlerClient.getRenderMob(this.entry, this.watch.player.getEntityWorld());
            this.parent.initPages(this.parent.pokemob);
        }));
        this.addButton(new Button(x - 65, y + 4, 20, 9, "\u2500", b ->
        { // Cycle Form.
            this.formes = Database.customModels.getOrDefault(this.entry, Collections.emptyList());
            final List<PokedexEntry> entries = Lists.newArrayList(Database.getFormes(this.entry));
            this.entryIndex = this.entryIndex % entries.size();
            if (!this.formes.isEmpty() && this.formIndex++ < this.formes.size()) this.holder = this.formes.get(
                    this.formIndex);
            if (this.entryIndex != -1)
            {
                this.formIndex = -1;
                this.entry = entries.get((this.entryIndex + 1) % entries.size());
                this.holder = this.entry.default_holder;
                this.parent.initPages(this.parent.pokemob.megaEvolve(this.entry));
            }
            this.parent.pokemob.setCustomHolder(this.holder);
        }));
        this.addButton(new Button(x - 65, y + 13, 20, 10, "\u266B", b ->
        {
            this.watch.player.playSound(this.parent.pokemob.getSound(), 0.5f, 1.0F);
        }));
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        this.drawInfo(mouseX, mouseY, partialTicks);
        super.render(mouseX, mouseY, partialTicks);
    }

}
