package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

public class AttainedDropsHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	static HashMap<ComparableItemStack, ItemStack[]> soilOutputMap = new HashMap<>();
	static HashMap<ComparableItemStack, IBlockState> bulbMap = new HashMap<>();

	@Override
	public void init()
	{
		Block blockPlant = Block.REGISTRY.getObject(new ResourceLocation("attaineddrops:plant"));
		final Item itemSeed = Item.REGISTRY.getObject(new ResourceLocation("attaineddrops:itemseed"));
		if(blockPlant==null||itemSeed==null)
			return;
		final IBlockState blockstatePlant = blockPlant.getDefaultState();
		IProperty propertyAge = null;
		for(IProperty prop : blockstatePlant.getPropertyNames())
			if("age".equals(prop.getName()) && prop instanceof PropertyInteger)
				propertyAge = prop;
		final IProperty propertyAge_final = propertyAge;

		addType("slimeball", new ItemStack(Items.SLIME_BALL));
		addType("bone", new ItemStack(Items.BONE));
		addType("string", new ItemStack(Items.STRING));
		addType("rottenflesh", new ItemStack(Items.ROTTEN_FLESH));
		addType("ghasttear", new ItemStack(Items.GHAST_TEAR));
		addType("spidereye", new ItemStack(Items.SPIDER_EYE));
		addType("prismarine", new ItemStack(Items.PRISMARINE_SHARD));
		addType("blaze", new ItemStack(Items.BLAZE_ROD));
		addType("gunpowder", new ItemStack(Items.GUNPOWDER));
		addType("witherskull", new ItemStack(Items.SKULL,1,1));
		addType("enderpearl", new ItemStack(Items.ENDER_PEARL));
		BelljarHandler.registerHandler(new IPlantHandler()
		{
			@Override
			public boolean isCorrectSoil(ItemStack seed, ItemStack soil)
			{
				return soil!=null&&soilOutputMap.containsKey(new ComparableItemStack(soil));
			}
			@Override
			public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return growth<.5?.003125f:.0015625f;
			}
			@Override
			public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return .5f;
			}
			@Override
			public ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
			{
				ItemStack[] out = soilOutputMap.get(new ComparableItemStack(soil));
				if(out==null)
					return new ItemStack[0];
				return out;
			}
			@Override
			public boolean isValid(ItemStack seed)
			{
				return seed!=null && seed.getItem()==itemSeed;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return new IBlockState[0];
			}
			@Override
			@SideOnly(Side.CLIENT)
			public float getRenderSize(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
			{
				return .875f;
			}
			@Override
			@SideOnly(Side.CLIENT)
			public boolean overrideRender(ItemStack seed, ItemStack soil, float growth, TileEntity tile, BlockRendererDispatcher blockRenderer)
			{
				IBlockState state = blockstatePlant.withProperty(propertyAge_final, growth>=.5?7:Math.min(7,Math.round(7*growth*2)));
				IBakedModel model = blockRenderer.getModelForState(state);
				GlStateManager.pushMatrix();
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
				GlStateManager.popMatrix();
				if(growth>=.5)
				{
					state = bulbMap.get(new ComparableItemStack(soil));
					model = blockRenderer.getModelForState(state);
					GlStateManager.pushMatrix();
					float scale = (growth-.5f)*2f;
					GlStateManager.translate(.5-scale/2, 1, -.5+scale/2);
					GlStateManager.scale(scale, scale, scale);
					blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, 1, true);
					GlStateManager.popMatrix();
				}
				return true;
			}
		});
	}

	static void addType(String type, ItemStack out)
	{
		Block soilBlock = Block.REGISTRY.getObject(new ResourceLocation("attaineddrops:"+type+"_soil"));
		Block bulbBlock = Block.REGISTRY.getObject(new ResourceLocation("attaineddrops:"+type+"_bulb"));
		if(soilBlock!=null && bulbBlock!=null)
		{
			ComparableItemStack comp = new ComparableItemStack(new ItemStack(soilBlock));
			soilOutputMap.put(comp, new ItemStack[]{out});
			bulbMap.put(comp, bulbBlock.getDefaultState());
		}
	}

	@Override
	public void postInit()
	{
	}
}