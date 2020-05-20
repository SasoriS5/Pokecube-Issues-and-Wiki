package pokecube.core.ai.brain;

import java.util.List;
import java.util.Optional;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.math.IPosWrapper;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import thut.api.entity.ai.VectorPosWrapper;
import thut.api.maths.Vector3;

public class BrainUtils
{
    public static LivingEntity getAttackTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.ATTACKTARGET)) return brain.getMemory(MemoryModules.ATTACKTARGET).get();
        else if (mobIn instanceof MobEntity) return ((MobEntity) mobIn).getAttackTarget();
        else return null;
    }

    public static boolean hasAttackTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getAttackTarget(mobIn) != null;
    }

    public static void setAttackTarget(final LivingEntity mobIn, final LivingEntity target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.ATTACKTARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.ATTACKTARGET, target);
        if (mobIn instanceof MobEntity) ((MobEntity) mobIn).setAttackTarget(target);
    }

    public static void setHuntTarget(final LivingEntity mobIn, final LivingEntity target)
    {
        Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.HUNTTARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.HUNTTARGET, target);
        BrainUtils.setAttackTarget(mobIn, target);
        if (target != null)
        {
            brain = target.getBrain();
            if (brain.hasMemory(MemoryModules.HUNTED_BY, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                    MemoryModules.HUNTED_BY, mobIn);
        }
    }

    public static LivingEntity getHuntTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.HUNTTARGET)) return brain.getMemory(MemoryModules.HUNTTARGET).get();
        else if (mobIn instanceof MobEntity) return ((MobEntity) mobIn).getAttackTarget();
        else return null;
    }

    public static boolean hasHuntTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getHuntTarget(mobIn) != null;
    }

    public static AgeableEntity getMateTarget(final AgeableEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.MATE_TARGET)) return brain.getMemory(MemoryModules.MATE_TARGET).get();
        else return null;
    }

    public static boolean hasMateTarget(final AgeableEntity mobIn)
    {
        return BrainUtils.getMateTarget(mobIn) != null;
    }

    public static void setMateTarget(final AgeableEntity mobIn, final AgeableEntity target)
    {
        final Brain<?> brain = mobIn.getBrain();
        if (brain.hasMemory(MemoryModules.MATE_TARGET, MemoryModuleStatus.REGISTERED)) brain.setMemory(
                MemoryModules.MATE_TARGET, target);
    }

    public static void lookAt(final LivingEntity entityIn, final double x, final double y, final double z)
    {
        BrainUtils.lookAt(entityIn, Vector3.getNewVector().set(x, y, z));
    }

    public static void lookAt(final LivingEntity entityIn, final Vector3 vec)
    {
        entityIn.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new VectorPosWrapper(vec));
    }

    public static void setMoveUseTarget(final LivingEntity mobIn, final Vector3 pos)
    {
        BrainUtils.setMoveUseTarget(mobIn, new VectorPosWrapper(pos));
    }

    public static void setMoveUseTarget(final LivingEntity mobIn, final IPosWrapper pos)
    {
        final Brain<?> brain = mobIn.getBrain();
        brain.setMemory(MemoryModules.MOVE_TARGET, pos);
    }

    public static void clearMoveUseTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        brain.removeMemory(MemoryModules.MOVE_TARGET);
    }

    public static boolean hasMoveUseTarget(final LivingEntity mobIn)
    {
        return BrainUtils.getMoveUseTarget(mobIn) != null;
    }

    public static IPosWrapper getMoveUseTarget(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        final Optional<IPosWrapper> pos = brain.getMemory(MemoryModules.MOVE_TARGET);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static List<NearBlock> getNearBlocks(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        final Optional<List<NearBlock>> pos = brain.getMemory(MemoryModules.VISIBLE_BLOCKS);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static List<ItemEntity> getNearItems(final LivingEntity mobIn)
    {
        final Brain<?> brain = mobIn.getBrain();
        final Optional<List<ItemEntity>> pos = brain.getMemory(MemoryModules.VISIBLE_ITEMS);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static List<AgeableEntity> getMates(final AgeableEntity entity)
    {
        final Brain<?> brain = entity.getBrain();
        final Optional<List<AgeableEntity>> pos = brain.getMemory(MemoryModules.POSSIBLE_MATES);
        if (pos == null || !pos.isPresent()) return null;
        return pos.get();
    }

    public static void addToBrain(final Brain<?> brain, final List<MemoryModuleType<?>> MEMORY_TYPES,
            final List<SensorType<?>> SENSOR_TYPES)
    {
        MEMORY_TYPES.forEach((module) ->
        {
            brain.memories.put(module, Optional.empty());
        });
        SENSOR_TYPES.forEach((type) ->
        {
            @SuppressWarnings("unchecked")
            final SensorType<? extends Sensor<? super LivingEntity>> stype = (SensorType<? extends Sensor<? super LivingEntity>>) type;
            @SuppressWarnings("unchecked")
            final Sensor<LivingEntity> sense = (Sensor<LivingEntity>) stype.func_220995_a();
            brain.sensors.put(stype, sense);
        });
        brain.sensors.values().forEach((sensor) ->
        {
            for (final MemoryModuleType<?> memorymoduletype : sensor.getUsedMemories())
                brain.memories.put(memorymoduletype, Optional.empty());
        });
    }
}