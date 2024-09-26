/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.crafting.fluidaware.BasicShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;

public class DecorationRecipes extends IERecipeProvider
{
	public DecorationRecipes(PackOutput p_248933_)
	{
		super(p_248933_);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		for(Entry<ResourceLocation, BlockEntry<SlabBlock>> blockSlab : IEBlocks.TO_SLAB.entrySet())
		{
			Block block = BuiltInRegistries.BLOCK.get(blockSlab.getKey());
			BlockEntry<SlabBlock> slab = blockSlab.getValue();
			shapedMisc(slab, 6)
					.define('s', block)
					.pattern("sss")
					.unlockedBy("has_"+toPath(block), has(block))
					.save(out, toRL(toPath(block)+"_to_slab"));
			shapedMisc(block)
					.define('s', slab)
					.pattern("s")
					.pattern("s")
					.unlockedBy("has_"+toPath(block), has(block))
					.save(out, toRL(toPath(block)+"_from_slab"));
		}
		stoneDecoration(out);
		woodenDecoration(out);
		metalDecorations(out);
	}

	private void woodenDecoration(RecipeOutput out)
	{
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			addStairs(WoodenDecoration.TREATED_WOOD.get(style), out);

		int numTreatedStyles = TreatedWoodStyles.values().length;
		for(TreatedWoodStyles from : TreatedWoodStyles.values())
		{
			TreatedWoodStyles to = TreatedWoodStyles.values()[(from.ordinal()+1)%numTreatedStyles];
			shapelessMisc(WoodenDecoration.TREATED_WOOD.get(to))
					.requires(WoodenDecoration.TREATED_WOOD.get(from))
					.unlockedBy("has_"+toPath(WoodenDecoration.TREATED_WOOD.get(from)), has(WoodenDecoration.TREATED_WOOD.get(from)))
					.save(out, toRL(toPath(WoodenDecoration.TREATED_WOOD.get(to))+"_from_"+from.toString().toLowerCase(Locale.US)));
		}
		shapedMisc(WoodenDecoration.TREATED_SCAFFOLDING, 6)
				.pattern("iii")
				.pattern(" s ")
				.pattern("s s")
				.define('i', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(WoodenDecoration.TREATED_SCAFFOLDING)));

		shapedMisc(WoodenDecoration.TREATED_FENCE, 3)
				.pattern("isi")
				.pattern("isi")
				.define('i', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(WoodenDecoration.TREATED_FENCE)));

		shapedMisc(WoodenDecoration.TREATED_FENCE_GATE)
				.pattern("sis")
				.pattern("sis")
				.define('i', IETags.getItemTag(IETags.treatedWood))
				.define('s', IETags.treatedStick)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.unlockedBy("has_treated_sticks", has(IETags.treatedStick))
				.save(out, toRL(toPath(WoodenDecoration.TREATED_FENCE_GATE)));

		shapedMisc(WoodenDecoration.TREATED_POST)
				.pattern("f")
				.pattern("f")
				.pattern("s")
				.define('f', WoodenDecoration.TREATED_FENCE)
				.define('s', Blocks.STONE_BRICKS)
				.unlockedBy("has_"+toPath(WoodenDecoration.TREATED_FENCE), has(WoodenDecoration.TREATED_FENCE))
				.save(out, toRL(toPath(WoodenDecoration.TREATED_POST)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL), 8)
				.pattern("www")
				.pattern("wbw")
				.pattern("www")
				.define('w', ItemTags.PLANKS)
				.define('b', new IngredientFluidStack(IETags.fluidCreosote, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_creosote", has(IEFluids.CREOSOTE.getBucket()))
				.save(
						new WrappingRecipeOutput<>(out, BasicShapedRecipe::new),
						toRL(toPath(WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL)))
				);

		shapedMisc(WoodenDecoration.SAWDUST, 9)
				.pattern("sss")
				.pattern("sss")
				.pattern("sss")
				.define('s', IETags.sawdust)
				.unlockedBy("has_sawdust", has(IETags.sawdust))
				.save(out, toRL(toPath(WoodenDecoration.SAWDUST)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.FIBERBOARD, 8)
				.pattern("www")
				.pattern("wbw")
				.pattern("www")
				.define('w', IETags.sawdust)
				.define('b', new IngredientFluidStack(IETags.fluidResin, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_resin", has(IEFluids.PHENOLIC_RESIN.getBucket()))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(WoodenDecoration.FIBERBOARD)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.WINDOW, 8)
				.pattern("wgw")
				.pattern("ggg")
				.pattern("wgw")
				.define('w', IETags.getItemTag(IETags.treatedWoodSlab))
				.define('g', Tags.Items.GLASS_PANES)
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(WoodenDecoration.WINDOW)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.CATWALK, 6)
				.pattern("rrr")
				.pattern("r r")
				.pattern("sss")
				.define('r', IETags.treatedStick)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(WoodenDecoration.CATWALK)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.CATWALK_STAIRS, 4)
				.pattern("s  ")
				.pattern("rs ")
				.pattern(" rs")
				.define('r', IETags.treatedStick)
				.define('s', IETags.getItemTag(IETags.treatedWoodSlab))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(WoodenDecoration.CATWALK_STAIRS)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.DOOR, 3)
				.pattern("ww")
				.pattern("ww")
				.pattern("ww")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(WoodenDecoration.DOOR)));
		addVariationChain(out, WoodenDecoration.DOOR, WoodenDecoration.DOOR_FRAMED);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WoodenDecoration.TRAPDOOR, 3)
				.pattern("www")
				.pattern("www")
				.define('w', IETags.getItemTag(IETags.treatedWood))
				.unlockedBy("has_treated_planks", has(IETags.getItemTag(IETags.treatedWood)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(WoodenDecoration.TRAPDOOR)));
		addVariationChain(out, WoodenDecoration.TRAPDOOR, WoodenDecoration.TRAPDOOR_FRAMED);
	}

	private void stoneDecoration(RecipeOutput out)
	{
		addCornerStraightMiddle(StoneDecoration.COKEBRICK, 3,
				makeIngredient(IETags.clay),
				makeIngredient(Tags.Items.INGOTS_BRICK),
				makeIngredient(Tags.Items.SANDSTONE),
				has(IETags.clay), out);
		addCornerStraightMiddle(StoneDecoration.BLASTBRICK, 3,
				makeIngredient(Tags.Items.INGOTS_NETHER_BRICK),
				makeIngredient(Tags.Items.INGOTS_BRICK),
				makeIngredient(Blocks.MAGMA_BLOCK),
				has(Tags.Items.INGOTS_BRICK), out);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.SLAG_BRICK, 4)
				.pattern("ss")
				.pattern("ss")
				.define('s', IETags.slag)
				.unlockedBy("has_slag", has(IETags.slag))
				.save(out, toRL(toPath(StoneDecoration.SLAG_BRICK)));
		SimpleCookingRecipeBuilder.smoking(Ingredient.of(Blocks.BRICKS), RecipeCategory.MISC, StoneDecoration.CLINKER_BRICK, 0.1f, standardSmeltingTime)
				.unlockedBy("has_bricks", has(Blocks.BRICKS))
				.save(out, toRL("smoking/"+toPath(StoneDecoration.CLINKER_BRICK)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.CLINKER_BRICK_QUOIN, 4)
				.pattern("tb")
				.pattern("bb")
				.define('t', Blocks.WHITE_TERRACOTTA)
				.define('b', StoneDecoration.CLINKER_BRICK)
				.unlockedBy("has_bricks", has(Blocks.BRICKS))
				.save(out, toRL(toPath(StoneDecoration.CLINKER_BRICK_QUOIN)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.CLINKER_BRICK_SILL, 4)
				.pattern("tt")
				.pattern("bb")
				.define('t', Blocks.WHITE_TERRACOTTA)
				.define('b', StoneDecoration.CLINKER_BRICK)
				.unlockedBy("has_bricks", has(Blocks.BRICKS))
				.save(out, toRL(toPath(StoneDecoration.CLINKER_BRICK_SILL)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.HEMPCRETE, 8)
				.pattern("scs")
				.pattern("tbt")
				.pattern("scs")
				.define('s', Tags.Items.SAND)
				.define('c', IETags.clay)
				.define('t', Ingredients.HEMP_FABRIC)
				.define('b', new IngredientFluidStack(FluidTags.WATER, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_clay", has(IETags.clay))
				.save(new WrappingRecipeOutput<ShapedRecipe>(
						out, r -> new TurnAndCopyRecipe(r, List.of()).allowQuarterTurn()
				), toRL("hempcrete"));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.HEMPCRETE_BRICK, 4)
				.pattern("hh")
				.pattern("hh")
				.define('h', StoneDecoration.HEMPCRETE)
				.unlockedBy("has_hempcrete", has(StoneDecoration.HEMPCRETE))
				.save(out, toRL(toPath(StoneDecoration.HEMPCRETE_BRICK)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.HEMPCRETE_PILLAR, 2)
				.pattern("h")
				.pattern("h")
				.define('h', StoneDecoration.HEMPCRETE)
				.unlockedBy("has_hempcrete", has(StoneDecoration.HEMPCRETE))
				.save(out, toRL(toPath(StoneDecoration.HEMPCRETE_PILLAR)));
		add3x3Conversion(StoneDecoration.COKE, IEItems.Ingredients.COAL_COKE, IETags.coalCoke, out);

		addStairs(StoneDecoration.SLAG_BRICK, out);
		addStairs(StoneDecoration.CLINKER_BRICK, out);
		addStairs(StoneDecoration.HEMPCRETE, out);
		addStairs(StoneDecoration.HEMPCRETE_BRICK, out);
		addStairs(StoneDecoration.CONCRETE, out);
		addStairs(StoneDecoration.CONCRETE_BRICK, out);
		addStairs(StoneDecoration.CONCRETE_TILE, out);
		addStairs(StoneDecoration.CONCRETE_LEADED, out);

		addWall(StoneDecoration.SLAG_BRICK, out);
		addWall(StoneDecoration.CLINKER_BRICK, out);

		addStonecuttingRecipe(Ingredients.SLAG, StoneDecoration.SLAG_BRICK, out);
		addStonecuttingRecipe(StoneDecoration.SLAG_BRICK, IEBlocks.TO_SLAB.get(StoneDecoration.SLAG_BRICK.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.SLAG_BRICK, IEBlocks.TO_STAIRS.get(StoneDecoration.SLAG_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.SLAG_BRICK, IEBlocks.TO_WALL.get(StoneDecoration.SLAG_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CLINKER_BRICK, IEBlocks.TO_SLAB.get(StoneDecoration.CLINKER_BRICK.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CLINKER_BRICK, IEBlocks.TO_STAIRS.get(StoneDecoration.CLINKER_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CLINKER_BRICK, IEBlocks.TO_WALL.get(StoneDecoration.CLINKER_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, IEBlocks.TO_SLAB.get(StoneDecoration.HEMPCRETE.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, IEBlocks.TO_STAIRS.get(StoneDecoration.HEMPCRETE.getId()), out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE_BRICK, IEBlocks.TO_SLAB.get(StoneDecoration.HEMPCRETE_BRICK.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE_BRICK, IEBlocks.TO_STAIRS.get(StoneDecoration.HEMPCRETE_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, IEBlocks.TO_SLAB.get(StoneDecoration.HEMPCRETE_BRICK.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, IEBlocks.TO_STAIRS.get(StoneDecoration.HEMPCRETE_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE_PILLAR, StoneDecoration.HEMPCRETE, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE_CHISELED, StoneDecoration.HEMPCRETE, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE_BRICK, StoneDecoration.HEMPCRETE, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_SHEET, 16, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_QUARTER, 4, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_THREE_QUARTER, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_BRICK, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_BRICK.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_BRICK, IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_TILE, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_TILE.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_TILE, IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE_TILE.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_BRICK.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE_BRICK.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_TILE.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE_TILE.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_LEADED, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_LEADED.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_LEADED, IEBlocks.TO_STAIRS.get(StoneDecoration.CONCRETE_LEADED.getId()), out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_REINFORCED, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_REINFORCED.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_REINFORCED_TILE, IEBlocks.TO_SLAB.get(StoneDecoration.CONCRETE_REINFORCED_TILE.getId()), 2, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, StoneDecoration.HEMPCRETE_BRICK, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, StoneDecoration.HEMPCRETE_CHISELED, out);
		addStonecuttingRecipe(StoneDecoration.HEMPCRETE, StoneDecoration.HEMPCRETE_PILLAR, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_BRICK, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_CHISELED, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_PILLAR, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_TILE, StoneDecoration.CONCRETE_BRICK, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_TILE, StoneDecoration.CONCRETE_CHISELED, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_TILE, StoneDecoration.CONCRETE_PILLAR, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_BRICK, StoneDecoration.CONCRETE_TILE, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_CHISELED, StoneDecoration.CONCRETE_TILE, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE_PILLAR, StoneDecoration.CONCRETE_TILE, out);
		addStonecuttingRecipe(StoneDecoration.CONCRETE, StoneDecoration.CONCRETE_TILE, out);

		SimpleCookingRecipeBuilder.smelting(Ingredient.of(StoneDecoration.CONCRETE_BRICK), RecipeCategory.MISC, StoneDecoration.CONCRETE_BRICK_CRACKED, 0.1f, standardSmeltingTime)
				.unlockedBy("has_concrete", has(StoneDecoration.CONCRETE))
				.save(out, toRL("smelting/"+toPath(StoneDecoration.CONCRETE_BRICK_CRACKED)));
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(StoneDecoration.HEMPCRETE_BRICK), RecipeCategory.MISC, StoneDecoration.HEMPCRETE_BRICK_CRACKED, 0.1f, standardSmeltingTime)
				.unlockedBy("has_hempcrete", has(StoneDecoration.HEMPCRETE))
				.save(out, toRL("smelting/"+toPath(StoneDecoration.HEMPCRETE_BRICK_CRACKED)));

		SimpleCookingRecipeBuilder.smelting(Ingredient.of(IETags.slag), RecipeCategory.MISC, StoneDecoration.SLAG_GLASS, 0.1f, standardSmeltingTime)
				.unlockedBy("has_slag", has(IETags.slag))
				.save(out, toRL("smelting/"+toPath(StoneDecoration.SLAG_GLASS)));

		shapedMisc(StoneDecoration.ALLOYBRICK, 2)
				.pattern("sb")
				.pattern("bs")
				.define('s', Tags.Items.SANDSTONE)
				.define('b', Tags.Items.INGOTS_BRICK)
				.unlockedBy("has_brick", has(Tags.Items.INGOTS_BRICK))
				.save(out, toRL(toPath(StoneDecoration.ALLOYBRICK)));
		shapelessMisc(StoneDecoration.BLASTBRICK_REINFORCED)
				.requires(StoneDecoration.BLASTBRICK)
				.requires(IETags.getTagsFor(EnumMetals.STEEL).plate)
				.unlockedBy("has_blastbrick", has(StoneDecoration.BLASTBRICK))
				.save(out, toRL(toPath(StoneDecoration.BLASTBRICK_REINFORCED)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, StoneDecoration.CONCRETE, 8)
				.group("ie_concrete")
				.pattern("scs")
				.pattern("gbg")
				.pattern("scs")
				.define('s', Tags.Items.SAND)
				.define('c', IETags.clay)
				.define('g', Tags.Items.GRAVEL)
				.define('b', new IngredientFluidStack(FluidTags.WATER, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_clay", has(IETags.clay))
				.save(new WrappingRecipeOutput<ShapedRecipe>(
						out, r -> new TurnAndCopyRecipe(r, List.of()).allowQuarterTurn()
				), toRL("concrete"));
		shapedMisc(StoneDecoration.CONCRETE_TILE, 4)
				.group("ie_concrete")
				.pattern("cc")
				.pattern("cc")
				.define('c', StoneDecoration.CONCRETE)
				.unlockedBy("has_concrete", has(StoneDecoration.CONCRETE))
				.save(out, toRL(toPath(StoneDecoration.CONCRETE_TILE)));
		shapedMisc(StoneDecoration.CONCRETE_BRICK, 4)
				.pattern("cc")
				.pattern("cc")
				.define('c', StoneDecoration.CONCRETE_TILE)
				.unlockedBy("has_concrete", has(StoneDecoration.CONCRETE))
				.save(out, toRL(toPath(StoneDecoration.CONCRETE_BRICK)));
		shapedMisc(StoneDecoration.CONCRETE_PILLAR, 2)
				.pattern("c")
				.pattern("c")
				.define('c', StoneDecoration.CONCRETE_TILE)
				.unlockedBy("has_concrete", has(StoneDecoration.CONCRETE))
				.save(out, toRL(toPath(StoneDecoration.CONCRETE_PILLAR)));
		shapelessMisc(StoneDecoration.CONCRETE_LEADED)
				.requires(StoneDecoration.CONCRETE)
				.requires(IETags.getTagsFor(EnumMetals.LEAD).plate)
				.unlockedBy("has_concrete", has(StoneDecoration.CONCRETE))
				.save(out, toRL(toPath(StoneDecoration.CONCRETE_LEADED)));
		shapedMisc(StoneDecoration.CONCRETE_REINFORCED, 4)
				.pattern("rrr")
				.pattern("rcr")
				.pattern("rrr")
				.define('r', IETags.netheriteRod)
				.define('c', new IngredientFluidStack(IETags.fluidConcrete, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
				.save(out, toRL(toPath(StoneDecoration.CONCRETE_REINFORCED)));
		shapedMisc(StoneDecoration.CONCRETE_REINFORCED_TILE, 4)
				.group("ie_concrete")
				.pattern("cc")
				.pattern("cc")
				.define('c', StoneDecoration.CONCRETE_REINFORCED)
				.unlockedBy("has_reinforced_concrete", has(StoneDecoration.CONCRETE_REINFORCED))
				.save(out, toRL(toPath(StoneDecoration.CONCRETE_REINFORCED_TILE)));
	}

	private void metalDecorations(RecipeOutput out)
	{
		for(DyeColor dye : DyeColor.values())
		{
			TagKey<Item> dyeTag = createItemWrapper(new ResourceLocation("forge", "dyes/"+dye.getName()));
			Block coloredSheetmetal = MetalDecoration.COLORED_SHEETMETAL.get(dye).get();
			shapedMisc(coloredSheetmetal, 8)
					.pattern("sss")
					.pattern("sds")
					.pattern("sss")
					.define('s', IETags.getItemTag(IETags.sheetmetals))
					.define('d', dyeTag)
					.unlockedBy("has_sheetmetal", has(IETags.getItemTag(IETags.sheetmetals)))
					.save(out, toRL(toPath(coloredSheetmetal)));
		}

		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			addStairs(MetalDecoration.STEEL_SCAFFOLDING.get(type), out);
			addStairs(MetalDecoration.ALU_SCAFFOLDING.get(type), out);
		}

		int numScaffoldingTypes = MetalScaffoldingType.values().length;
		for(MetalScaffoldingType from : MetalScaffoldingType.values())
		{
			MetalScaffoldingType to = MetalScaffoldingType.values()[(from.ordinal()+1)%numScaffoldingTypes];
			shapelessMisc(MetalDecoration.ALU_SCAFFOLDING.get(to))
					.requires(MetalDecoration.ALU_SCAFFOLDING.get(from))
					.unlockedBy("has_"+toPath(MetalDecoration.ALU_SCAFFOLDING.get(from)), has(MetalDecoration.ALU_SCAFFOLDING.get(from)))
					.save(out, toRL("alu_scaffolding_"+to.name().toLowerCase(Locale.US)+"_from_"+from.name().toLowerCase(Locale.US)));
			shapelessMisc(MetalDecoration.STEEL_SCAFFOLDING.get(to))
					.requires(MetalDecoration.STEEL_SCAFFOLDING.get(from))
					.unlockedBy("has_"+toPath(MetalDecoration.STEEL_SCAFFOLDING.get(from)), has(MetalDecoration.STEEL_SCAFFOLDING.get(from)))
					.save(out, toRL("steel_scaffolding_"+to.name().toLowerCase(Locale.US)+"_from_"+from.name().toLowerCase(Locale.US)));
		}

		shapedMisc(MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), 6)
				.pattern("iii")
				.pattern(" s ")
				.pattern("s s")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.unlockedBy("has_alu_sticks", has(IETags.aluminumRod))
				.save(out, toRL(toPath(MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD))));
		shapedMisc(MetalDecoration.ALU_SLOPE, 4)
				.pattern("sss")
				.pattern("ss ")
				.pattern("s  ")
				.define('s', MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD))
				.unlockedBy("has_"+toPath(MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)), has(MetalDecoration.ALU_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)))
				.save(out, toRL(toPath(MetalDecoration.ALU_SLOPE)));

		shapedMisc(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD), 6)
				.pattern("iii")
				.pattern(" s ")
				.pattern("s s")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_steel_sticks", has(IETags.steelRod))
				.save(out, toRL(toPath(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD))));
		shapedMisc(MetalDecoration.STEEL_SLOPE, 4)
				.pattern("sss")
				.pattern("ss ")
				.pattern("s  ")
				.define('s', MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD))
				.unlockedBy("has_"+toPath(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)), has(MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)))
				.save(out, toRL(toPath(MetalDecoration.STEEL_SLOPE)));

		shapedMisc(MetalDecoration.ALU_FENCE, 3)
				.pattern("isi")
				.pattern("isi")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.unlockedBy("has_alu_sticks", has(IETags.aluminumRod))
				.save(out, toRL(toPath(MetalDecoration.ALU_FENCE)));
		shapedMisc(MetalDecoration.STEEL_FENCE, 3)
				.pattern("isi")
				.pattern("isi")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_steel_sticks", has(IETags.steelRod))
				.save(out, toRL(toPath(MetalDecoration.STEEL_FENCE)));

		shapedMisc(MetalDecoration.ALU_FENCE_GATE)
				.pattern("sis")
				.pattern("sis")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_alu_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.unlockedBy("has_alu_sticks", has(IETags.aluminumRod))
				.save(out, toRL(toPath(MetalDecoration.ALU_FENCE_GATE)));
		shapedMisc(MetalDecoration.STEEL_FENCE_GATE)
				.pattern("sis")
				.pattern("sis")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_steel_sticks", has(IETags.steelRod))
				.save(out, toRL(toPath(MetalDecoration.STEEL_FENCE_GATE)));

		shapedMisc(MetalDecoration.LANTERN)
				.pattern("iii")
				.pattern("pgp")
				.pattern("iii")
				.define('i', IETags.getTagsFor(EnumMetals.IRON).nugget)
				.define('g', Tags.Items.DUSTS_GLOWSTONE)
				.define('p', Items.GLASS_PANE)
				.unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
				.save(out, toRL(toPath(MetalDecoration.LANTERN)));
		shapedMisc(MetalDecoration.CAGELAMP)
				.pattern(" c ")
				.pattern("crc")
				.pattern("igi")
				.define('c', IETags.copperWire)
				.define('r', Tags.Items.GLASS_RED)
				.define('i', IETags.getTagsFor(EnumMetals.IRON).nugget)
				.define('g', Tags.Items.DUSTS_GLOWSTONE)
				.unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
				.save(out, toRL(toPath(MetalDecoration.CAGELAMP)));

		shapedMisc(MetalDecoration.LV_COIL)
				.pattern("www")
				.pattern("wiw")
				.pattern("www")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('w', Misc.WIRE_COILS.get(WireType.COPPER))
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.unlockedBy("has_"+toPath(Misc.WIRE_COILS.get(WireType.COPPER)), has(Misc.WIRE_COILS.get(WireType.COPPER)))
				.save(out, toRL(toPath(MetalDecoration.LV_COIL)));
		shapedMisc(MetalDecoration.MV_COIL)
				.pattern("www")
				.pattern("wiw")
				.pattern("www")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('w', Misc.WIRE_COILS.get(WireType.ELECTRUM))
				.unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot))
				.unlockedBy("has_"+toPath(Misc.WIRE_COILS.get(WireType.ELECTRUM)), has(Misc.WIRE_COILS.get(WireType.ELECTRUM)))
				.save(out, toRL(toPath(MetalDecoration.MV_COIL)));
		shapedMisc(MetalDecoration.HV_COIL)
				.pattern("www")
				.pattern("wiw")
				.pattern("www")
				.define('i', Tags.Items.INGOTS_IRON)
				.define('w', Misc.WIRE_COILS.get(WireType.STEEL))
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.unlockedBy("has_"+toPath(Misc.WIRE_COILS.get(WireType.STEEL)), has(Misc.WIRE_COILS.get(WireType.STEEL)))
				.save(out, toRL(toPath(MetalDecoration.HV_COIL)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.ENGINEERING_RS, 4)
				.pattern("iri")
				.pattern("rcr")
				.pattern("iri")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('r', Tags.Items.DUSTS_REDSTONE)
				.unlockedBy("has_iron_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_redstone", has(Items.REDSTONE))
				.save(
						new WrappingRecipeOutput<ShapedRecipe>(out, r -> new TurnAndCopyRecipe(r).allowEighthTurn()),
						toRL(toPath(MetalDecoration.ENGINEERING_RS))
				);
		shapedMisc(MetalDecoration.ENGINEERING_LIGHT, 4)
				.pattern("igi")
				.pattern("gcg")
				.pattern("igi")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
				.define('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.define('g', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_iron_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)))
				.unlockedBy("has_copper_ingot", has(IETags.getTagsFor(EnumMetals.COPPER).ingot))
				.unlockedBy("has_component_iron", has(Ingredients.COMPONENT_IRON))
				.save(out, toRL(toPath(MetalDecoration.ENGINEERING_LIGHT)));
		shapedMisc(MetalDecoration.ENGINEERING_HEAVY, 4)
				.pattern("igi")
				.pattern("geg")
				.pattern("igi")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
				.define('g', Ingredients.COMPONENT_STEEL)
				.unlockedBy("has_steel_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.unlockedBy("has_component_steel", has(Ingredients.COMPONENT_STEEL))
				.save(out, toRL(toPath(MetalDecoration.ENGINEERING_HEAVY)));
		shapedMisc(MetalDecoration.GENERATOR, 4)
				.pattern("ici")
				.pattern("cgc")
				.pattern("ici")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('c', MetalDecoration.MV_COIL)
				.define('g', Ingredients.COMPONENT_IRON)
				.unlockedBy("has_steel_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.unlockedBy("has_electrum_ingot", has(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
				.unlockedBy("has_component_iron", has(Ingredients.COMPONENT_IRON))
				.save(out, toRL(toPath(MetalDecoration.GENERATOR)));
		shapedMisc(MetalDecoration.RADIATOR, 4)
				.pattern("ici")
				.pattern("cbc")
				.pattern("ici")
				.define('i', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('c', IETags.getTagsFor(EnumMetals.CONSTANTAN).plate)
				.define('b', new IngredientFluidStack(FluidTags.WATER, FluidType.BUCKET_VOLUME))
				.unlockedBy("has_steel_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.unlockedBy("has_water_bucket", has(Items.WATER_BUCKET))
				.unlockedBy("has_constantan_ingot", has(IETags.getTagsFor(EnumMetals.CONSTANTAN).ingot))
				.save(
						new WrappingRecipeOutput<ShapedRecipe>(out, r -> new TurnAndCopyRecipe(r).allowQuarterTurn()),
						toRL(toPath(MetalDecoration.RADIATOR))
				);

		shapedMisc(MetalDecoration.ALU_POST)
				.pattern("f")
				.pattern("f")
				.pattern("s")
				.define('f', MetalDecoration.ALU_FENCE)
				.define('s', Blocks.STONE_BRICKS)
				.unlockedBy("has_"+toPath(MetalDecoration.ALU_FENCE), has(MetalDecoration.ALU_FENCE))
				.save(out, toRL(toPath(MetalDecoration.ALU_POST)));
		shapedMisc(MetalDecoration.STEEL_POST)
				.pattern("f")
				.pattern("f")
				.pattern("s")
				.define('f', MetalDecoration.STEEL_FENCE)
				.define('s', Blocks.STONE_BRICKS)
				.unlockedBy("has_"+toPath(MetalDecoration.STEEL_FENCE), has(MetalDecoration.STEEL_FENCE))
				.save(out, toRL(toPath(MetalDecoration.STEEL_POST)));

		shapedMisc(MetalDecoration.ALU_WALLMOUNT, 4)
				.pattern("ii")
				.pattern("is")
				.define('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
				.define('s', IETags.aluminumRod)
				.unlockedBy("has_aluminum_ingot", has(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
				.save(out, toRL(toPath(MetalDecoration.ALU_WALLMOUNT)));
		shapedMisc(MetalDecoration.STEEL_WALLMOUNT, 4)
				.pattern("ii")
				.pattern("is")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.define('s', IETags.steelRod)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(out, toRL(toPath(MetalDecoration.STEEL_WALLMOUNT)));

		shapedMisc(MetalDecoration.METAL_LADDER.get(CoverType.NONE), 3)
				.pattern("s s")
				.pattern("sss")
				.pattern("s s")
				.define('s', IETags.metalRods)
				.unlockedBy("has_metal_rod", has(IETags.metalRods))
				.save(out, toRL(toPath(MetalDecoration.METAL_LADDER.get(CoverType.NONE))));
		shapedMisc(MetalDecoration.METAL_LADDER.get(CoverType.ALU), 3)
				.pattern("s")
				.pattern("l")
				.define('s', IETags.getItemTag(IETags.scaffoldingAlu))
				.define('l', MetalDecoration.METAL_LADDER.get(CoverType.NONE))
				.unlockedBy("has_metal_ladder", has(MetalDecoration.METAL_LADDER.get(CoverType.NONE)))
				.save(out, toRL(toPath(MetalDecoration.METAL_LADDER.get(CoverType.ALU))));
		shapedMisc(MetalDecoration.METAL_LADDER.get(CoverType.STEEL), 3)
				.pattern("s")
				.pattern("l")
				.define('s', IETags.getItemTag(IETags.scaffoldingSteel))
				.define('l', MetalDecoration.METAL_LADDER.get(CoverType.NONE))
				.unlockedBy("has_metal_ladder", has(MetalDecoration.METAL_LADDER.get(CoverType.NONE)))
				.save(out, toRL(toPath(MetalDecoration.METAL_LADDER.get(CoverType.STEEL))));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.STEEL_WINDOW, 8)
				.pattern("rgr")
				.pattern("ggg")
				.pattern("rgr")
				.define('r', IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal))
				.define('g', Tags.Items.GLASS_PANES)
				.unlockedBy("has_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).sheetmetal)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.STEEL_WINDOW)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.ALU_WINDOW, 8)
				.pattern("rgr")
				.pattern("ggg")
				.pattern("rgr")
				.define('r', IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).sheetmetal))
				.define('g', Tags.Items.GLASS_PANES)
				.unlockedBy("has_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.ALUMINUM).sheetmetal)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.ALU_WINDOW)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.REINFORCED_WINDOW, 4)
				.pattern("rgr")
				.pattern("ddd")
				.pattern("rgr")
				.define('r', IETags.netheriteRod)
				.define('g', Tags.Items.GLASS_PANES)
				.define('d', IETags.plasticPlate)
				.unlockedBy("has_duroplast", has(IETags.plasticPlate))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.REINFORCED_WINDOW)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.STEEL_CATWALK, 6)
				.pattern("rrr")
				.pattern("r r")
				.pattern("sss")
				.define('r', IETags.steelRod)
				.define('s', IETags.getItemTag(IETags.scaffoldingSteelSlab))
				.unlockedBy("has_scaffolding", has(IETags.getItemTag(IETags.scaffoldingSteel)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.STEEL_CATWALK)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.STEEL_CATWALK_STAIRS, 4)
				.pattern("s  ")
				.pattern("rs ")
				.pattern(" rs")
				.define('r', IETags.steelRod)
				.define('s', IETags.getItemTag(IETags.scaffoldingSteelSlab))
				.unlockedBy("has_scaffolding", has(IETags.getItemTag(IETags.scaffoldingSteel)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.STEEL_CATWALK_STAIRS)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.ALU_CATWALK, 6)
				.pattern("rrr")
				.pattern("r r")
				.pattern("sss")
				.define('r', IETags.aluminumRod)
				.define('s', IETags.getItemTag(IETags.scaffoldingAluSlab))
				.unlockedBy("has_scaffolding", has(IETags.getItemTag(IETags.scaffoldingAlu)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.ALU_CATWALK)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.ALU_CATWALK_STAIRS, 4)
				.pattern("s  ")
				.pattern("rs ")
				.pattern(" rs")
				.define('r', IETags.aluminumRod)
				.define('s', IETags.getItemTag(IETags.scaffoldingAluSlab))
				.unlockedBy("has_scaffolding", has(IETags.getItemTag(IETags.scaffoldingAlu)))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.ALU_CATWALK_STAIRS)));

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.STEEL_DOOR, 3)
				.pattern("ii")
				.pattern("ii")
				.pattern("ii")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.STEEL_DOOR)));
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MetalDecoration.STEEL_TRAPDOOR, 3)
				.pattern("iii")
				.pattern("iii")
				.define('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
				.unlockedBy("has_steel_ingot", has(IETags.getTagsFor(EnumMetals.STEEL).ingot))
				.save(new WrappingRecipeOutput<>(out, BasicShapedRecipe::new), toRL(toPath(MetalDecoration.STEEL_TRAPDOOR)));
	}

	private void addStairs(ItemLike block, RecipeOutput out)
	{
		addStairs(block, IEBlocks.TO_STAIRS.get(BuiltInRegistries.ITEM.getKey(block.asItem())), out);
	}

	private void addStairs(ItemLike block, ItemLike stairs, RecipeOutput out)
	{
		shapedMisc(stairs, 4)
				.define('s', block)
				.pattern("s  ")
				.pattern("ss ")
				.pattern("sss")
				.unlockedBy("has_"+toPath(block), has(block))
				.save(out, toRL(toPath(stairs)));
	}

	private void addVariationChain(RecipeOutput out, ItemLike... items)
	{
		for(int from = 0; from < items.length; from++)
		{
			int to = (from+1)%items.length;
			shapelessMisc(items[to])
					.requires(items[from])
					.unlockedBy("has_"+toPath(items[from]), has(items[from]))
					.save(out, toRL(toPath(items[to])+"_from_"+toPath(items[from])));
		}
	}
}
