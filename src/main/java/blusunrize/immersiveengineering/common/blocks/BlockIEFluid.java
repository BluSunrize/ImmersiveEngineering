package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * @author BluSunrize - 04.08.2016
 */
public class BlockIEFluid extends BlockFluidClassic
{
	public BlockIEFluid(String name, Fluid fluid, Material material)
	{
		super(fluid, material);
		this.setUnlocalizedName(ImmersiveEngineering.MODID + "." + name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		ImmersiveEngineering.registerBlock(this, ItemBlock.class, name);
		IEContent.registeredIEBlocks.add(this);
	}
}
