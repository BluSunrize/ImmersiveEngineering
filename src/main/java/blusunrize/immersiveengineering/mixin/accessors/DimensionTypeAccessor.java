package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor
{
	@Accessor("OVERWORLD_TYPE")
	static DimensionType getOverworldType()
	{
		throw new UnsupportedOperationException("Replaced by Mixin");
	}
}
