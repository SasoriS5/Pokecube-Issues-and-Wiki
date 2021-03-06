package pokecube.legends.conditions;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokemobTracker;
import pokecube.legends.PokecubeLegends;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public abstract class Condition implements ISpecialCaptureCondition, ISpecialSpawnCondition
{

    protected static boolean isBlock(final World world, final ArrayList<Vector3> blocks, final Block toTest)
    {
        for (final Vector3 v : blocks)
            if (v.getBlock(world) != toTest) return false;
        return true;
    }

    protected static boolean isBlock(final World world, final ArrayList<Vector3> blocks, final ResourceLocation toTest)
    {
        for (final Vector3 v : blocks)
            if (!ItemList.is(toTest, v.getBlockState(world))) return false;
        return true;
    }

    /**
     * @param world
     * @param blocks
     * @param material
     * @param bool
     *            if true, looks for matches, if false looks for anything that
     *            doesn't match.
     * @return
     */
    protected static boolean isMaterial(final World world, final ArrayList<Vector3> blocks, final Material material,
            final boolean bool)
    {
        final boolean ret = true;
        if (bool)
        {
            for (final Vector3 v : blocks)
                if (v.getBlockMaterial(world) != material) return false;
        }
        else for (final Vector3 v : blocks)
            if (v.getBlockMaterial(world) == material) return false;
        return ret;
    }

    public abstract PokedexEntry getEntry();

    @Override
    public boolean canCapture(final Entity trainer)
    {
        if (trainer == null) return false;
        if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), this.getEntry()) > 0) return false;
        if (trainer instanceof ServerPlayerEntity && PokecubePlayerDataHandler.getCustomDataTag(
                (ServerPlayerEntity) trainer).getBoolean("capt:" + this.getEntry().getTrimmedName())) return false;
        return true;
    }

    @Override
    public void onSpawn(final IPokemob mob)
    {
    }

    @Override
    public CanSpawn canSpawn(final Entity trainer)
    {
        if (trainer == null) return CanSpawn.NO;
        // Already have one, cannot spawn again.
        if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), this.getEntry()) > 0)
            return CanSpawn.ALREADYHAVE;
        // Also check if can capture, if not, no point in being able to spawn.
        if (trainer instanceof ServerPlayerEntity && PokecubePlayerDataHandler.getCustomDataTag(
                (ServerPlayerEntity) trainer).getBoolean("capt:" + this.getEntry().getTrimmedName()))
            return CanSpawn.ALREADYHAVE;
        if (trainer instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) trainer;
            final String tag0 = "spwn:" + this.getEntry().getTrimmedName();
            final boolean prevSpawn = PokecubePlayerDataHandler.getCustomDataTag(player).getBoolean(tag0);
            if (!prevSpawn) return CanSpawn.YES;
            final MinecraftServer server = trainer.getServer();
            final String tag1 = "spwn_ded:" + this.getEntry().getTrimmedName();
            final long spwnDied = PokecubePlayerDataHandler.getCustomDataTag(player).getLong(tag1);
            final boolean prevDied = spwnDied > 0;
            if (prevDied)
            {
                final boolean doneCooldown = spwnDied + PokecubeLegends.config.respawnLegendDelay < server.getWorld(
                        DimensionType.OVERWORLD).getGameTime();
                if (doneCooldown)
                {
                    PokecubePlayerDataHandler.getCustomDataTag(player).remove(tag0);
                    PokecubePlayerDataHandler.getCustomDataTag(player).remove(tag1);
                    PokecubePlayerDataHandler.saveCustomData(player);
                    return CanSpawn.YES;
                }
            }
            return CanSpawn.ALREADYHAVE;
        }
        return CanSpawn.YES;
    }

    @Override
    public CanSpawn canSpawn(final Entity trainer, final Vector3 location, final boolean message)
    {
        final CanSpawn test = this.canSpawn(trainer);
        if (!test.test()) return test;
        final SpawnData data = this.getEntry().getSpawnData();
        final boolean canSpawnHere = data == null || SpawnHandler.canSpawn(this.getEntry().getSpawnData(), location,
                trainer.getEntityWorld(), false);
        if (canSpawnHere)
        {
            final boolean here = PokemobTracker.countPokemobs(location, trainer.getEntityWorld(), 32, this
                    .getEntry()) > 0;
            return here ? CanSpawn.ALREADYHERE : CanSpawn.YES;
        }
        if (message) this.sendNoHere(trainer);
        return CanSpawn.NOTHERE;
    }

    @Override
    public boolean canCapture(final Entity trainer, final IPokemob pokemon)
    {
        if (pokemon.getEntity().getPersistentData().contains("spwnedby:Most"))
        {
            final UUID id = pokemon.getEntity().getPersistentData().getUniqueId("spwnedby:");
            if (!trainer.getUniqueID().equals(id)) return false;
        }
        return this.canCapture(trainer);
    }

    public void sendNoTrust(final Entity trainer)
    {
        final String message = "msg.notrust.info";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }

    public void sendNoHere(final Entity trainer)
    {
        final String message = "msg.nohere.info";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }

    // Basic Legend
    public void sendLegend(final Entity trainer, final String type, final int numA, final int numB)
    {
        final String message = "msg.infolegend.info";
        final ITextComponent typeMess = new TranslationTextComponent(PokeType.getUnlocalizedName(PokeType.getType(
                type)));
        trainer.sendMessage(new TranslationTextComponent(message, typeMess, numA + 1, numB));
    }

    // Duo Type Legend
    public void sendLegendDuo(final Entity trainer, final String type, final String kill, final int numA,
            final int numB, final int killa, final int killb)
    {
        final String message = "msg.infolegendduo.info";
        final ITextComponent typeMess = new TranslationTextComponent(PokeType.getUnlocalizedName(PokeType.getType(
                type)));
        final ITextComponent killMess = new TranslationTextComponent(PokeType.getUnlocalizedName(PokeType.getType(
                kill)));
        trainer.sendMessage(new TranslationTextComponent(message, typeMess, killMess, numA + 1, numB, killa + 1,
                killb));
    }

    // Catch specific Legend
    public void sendLegendExtra(final Entity trainer, final String names)
    {
        final String message = "msg.infolegendextra.info";
        final String[] split = names.split(", ");
        ITextComponent namemes = null;
        for (final String s : split)
        {
            PokedexEntry entry = Database.getEntry(s);
            if (entry == null) entry = Database.missingno;
            if (namemes == null) namemes = new TranslationTextComponent(entry.getUnlocalizedName());
            else namemes = namemes.appendText(", ").appendSibling(new TranslationTextComponent(entry
                    .getUnlocalizedName()));
        }
        trainer.sendMessage(new TranslationTextComponent(message, namemes));
    }

    public void sendAngered(final Entity trainer)
    {
        final String message = "msg.angeredlegend.json";
        final ITextComponent component = new TranslationTextComponent(message, new TranslationTextComponent(this
                .getEntry().getUnlocalizedName()));
        trainer.sendMessage(component);
    }
}
