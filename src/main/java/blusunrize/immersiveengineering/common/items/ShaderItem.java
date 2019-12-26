/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.*;
import blusunrize.immersiveengineering.api.shader.ShaderCase.DynamicShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

//TODO one item per shader?
public class ShaderItem extends IEBaseItem implements IShaderItem, ITextureOverride
{
	public ShaderItem()
	{
		super("shader", new Properties().maxStackSize(1));
		//DEFAULT CUTOUTS
		//whitestripe
		setDefaultTextureBounds(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_whitestripe"), 0, 0, .25, .25);
		setDefaultTextureBounds(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_whitestripe"), 0, 22/64d, .5, 54/64d);

		//REGISTER SHADERS
		addShader("rosequartz", 0, Rarity.COMMON, 0xff412323, 0xffe6b4b4, 0xfff0cdcd, 0xffe6b4b4).setInfo(null, null, "rosequartz");
		addShader("argo", 2, Rarity.COMMON, 0xff2d2d2d, 0xffdcdcdc, 0xffdc7823, 0xffc8c8c8).setInfo(null, null, "argo");
		addShader("sunstrike", 5, Rarity.RARE, 0xff737373, 0xffcd6900, 0xb9d73a00, 0xb9d73a00).setInfo(null, null, "sunstrike");
		addShader("locus", 2, Rarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff84964c, 0xff4a4a4a).setInfo("Mercenaries", "Red vs Blue", "locus");
		addShader("felix", 1, Rarity.COMMON, 0xff0a0a0a, 0xff4a4a4a, 0xfff08803, 0xff4a4a4a).setInfo("Mercenaries", "Red vs Blue", "felix");
		addShader("sharkface", 2, Rarity.UNCOMMON, 0xff0a0a0a, 0xff4a4a4a, 0xff910008, 0xff4a4a4a, "shark", true, 0xffffffff).setInfo("Mercenaries", "Red vs Blue", "sharkface");
		addShader("dragonsbreath", 1, Rarity.UNCOMMON, 0xff191919, 0xff333f2b, 0xff8a8a8a, 0xff8a8a8a, "shark", true, 0xffffffff).setInfo(null, "Destiny", "dragonsbreath");
		addShader("hawk", 3, Rarity.COMMON, 0xff67636b, 0xfff4eeeb, 0xff2d2d2d, 0xfff4eeeb).setInfo(null, "Destiny", "hawk");
		addShader("eyas", 3, Rarity.COMMON, 0xff52534e, 0xff7a2a08, 0xff2d2d2d, 0xfff4eeeb).setInfo(null, "Destiny", "eyas");
		addShader("magnum", 1, Rarity.COMMON, 0xff56382c, 0xffdcdcdc, 0xffa0a0a0, 0xffdcdcdc).setInfo(null, null, "magnum");
		addShader("fox", 2, Rarity.UNCOMMON, 0xff2d2d2d, 0xffd47e31, 0xffeaeaea, 0xffeaeaea).setInfo(null, null, "fox");
		addShader("vaulttec", 0, Rarity.COMMON, 0xff56382c, 0xff1a4785, 0xffc0aa50, 0xffaaaaaa).setInfo(null, "Fallout", "vaulttec");

		ShaderRegistryEntry entry = addShader("sponsor", 0, Rarity.EPIC, 0xff191919, 0xfff71b24, 0xffffffff, 0xffaaaaaa, "sponsor", false, 0xffffffff).setInfo(null, "Fallout", "sponsor");
		((ShaderCaseMinecart)entry.getCase(new ResourceLocation("immersiveengineering", "minecart"))).mirrorSideForPass[2] = false;
		entry.getCase(new ResourceLocation("immersiveengineering", "revolver")).getLayers()[4].setTextureBounds(0, 0, .25, .25);
		entry.getCase(new ResourceLocation("immersiveengineering", "drill")).getLayers()[3].setTextureBounds(10/64d, 34/64d, 26/64d, 50/64d);
		entry.getCase(new ResourceLocation("immersiveengineering", "chemthrower")).getLayers()[3].setTextureBounds(6/64d, 16/64d, 22/64d, 32/64d);
		entry.getCase(new ResourceLocation("immersiveengineering", "shield")).getLayers()[2].setTextureBounds(0/32d, 9/32d, 14/32d, 25/32d).setCutoutBounds(.0625, 0, .9375, 1);

		addShader("massfusion", 3, Rarity.RARE, 0xff6e5a37, 0xff394730, 0xff545454, 0xffaaaaaa, "fusion", true, 0xffffffff).setInfo(null, "Fallout", "massfusion");

		addShader("stormflower", 1, Rarity.COMMON, 0xff273427, 0xff286f30, 0xff4b9255, 0xff286f30).setInfo(null, "RWBY", "stormflower");
		addShader("milo", 2, Rarity.UNCOMMON, 0xff3b1b10, 0xff670004, 0xffce7e10, 0xff670004).setInfo(null, "RWBY", "milo");
		addShader("trident", 2, Rarity.UNCOMMON, 0xff515151, 0xffa8a8a8, 0xff29d3ff, 0xffafafaf).setInfo(null, "RWBY", "trident");
		addShader("chloris", 4, Rarity.RARE, 0xff38322a, 0xff38322a, 0xff88fabe, 0xffc8c8c8).setInfo(null, "RWBY", "chloris");
		addShader("crescentrose", 2, Rarity.COMMON, 0xff141414, 0xff910008, 0xff080808, 0xffa4a4a4).setInfo(null, "RWBY", "crescentrose");
		addShader("qrow", 2, Rarity.UNCOMMON, 0xff6d1c11, 0xffd8d7d0, 0xff313640, 0xff730008).setInfo(null, "RWBY", "qrow");
		entry = addShader("lususnaturae", 0, Rarity.EPIC, 0xff141919, 0xff141919, 0xffadb4bf, 0xffadb4bf).setInfo(null, "RWBY", "lususnaturae");
		addBlockScaledLayer(entry, "minecraft:block/destroy_stage_8", 0xbb940c0c);
		addLayer(entry, "1_4", 0xffadb4bf);

		addShader("vanguard", 3, Rarity.UNCOMMON, 0xff373737, 0xff131b42, 0xffb86c14, 0xffdcdcdc).setInfo(null, "Destiny", "vanguard");
		addShader("regal", 4, Rarity.UNCOMMON, 0xffd8d4d1, 0xff431c1d, 0xffd8d4d1, 0xffd8d4d1).setInfo(null, "Destiny", "regal");
		addShader("harrowed", 4, Rarity.RARE, 0xff161321, 0xff431c1d, 0xff161321, 0xff161321).setInfo(null, "Destiny", "harrowed");
		addShader("taken", 5, Rarity.EPIC, 0xff111c26, 0xff111c26, 0xffbad7dd, 0xff111c26, null, false, 0xffffffff).setInfo(null, "Destiny", "taken");
		entry = addShader("ikelos", 2, Lib.RARITY_Masterwork, 0xff74665d, 0xff424348, 0xff424348, 0xff313131).setInfo(null, "Destiny", "ikelos");
		addDynamicLayer(entry, "circuit", 0xffefa117,
				(layer, superColour) -> ClientUtils.pulseRGBAlpha(superColour, 40, .15f, 1f),
				(pre, partialTick) -> {
					//TODO are push/pop Attributes broken?
					if(pre)
					{
						GlStateManager.pushLightingAttributes();
						GL11.glLightfv(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(.5f, .2f, 0, .5f));
						ClientUtils.toggleLightmap(true, true);
					}
					else
					{
						ClientUtils.toggleLightmap(false, true);
						GlStateManager.popAttributes();
					}
				});
		addLayer(entry, "1_4", 0xff5f646a);
		entry.setEffectFunction((world, shader, item, shaderType, pos, dir, scale) -> {
			ImmersiveEngineering.proxy.spawnFractalFX(world, pos.x, pos.y, pos.z, dir!=null?dir: new Vec3d(0, 1, 0), scale, 2, null);
			ImmersiveEngineering.proxy.spawnFractalFX(world, pos.x, pos.y, pos.z, dir!=null?dir: new Vec3d(0, 0, 1), scale, 2, null);
			ImmersiveEngineering.proxy.spawnFractalFX(world, pos.x, pos.y, pos.z, dir!=null?dir: new Vec3d(1, 0, 0), scale, 2, null);
		});

		addShader("angelsthesis", 2, Rarity.EPIC, 0xff1e1e1e, 0xff754697, 0xff77b93d, 0xff505050, null, false, 0xffffffff).setInfo("Mecha", "Neon Genesis Evangelion", "angelsthesis");
		addShader("sutherland", 0, Rarity.RARE, 0xff44404f, 0xff6b5eae, 0xff702739, 0xff44404f, "whitestripe", true, 0xff702034).setInfo("Mecha", "Code Geass", "sutherland");
		addShader("exia", 8, Rarity.RARE, 0xffb2220c, 0xff5571d2, 0xffece7e1, 0xffc0fdc7, "whitestripe", true, 0xffc09032).setInfo("Mecha", "Gundam 00", "exia");
		addShader("crimsonlotus", 3, Rarity.EPIC, 0xffd83239, 0xffd83239, 0xff4e4f53, 0xff2ff177, "whitestripe", true, 0xfff4b951).setInfo("Mecha", "Gurren Lagann", "crimsonlotus");
		addShader("dominator", 1, Rarity.UNCOMMON, 0xff4c311f, 0xff2a2c36, 0xff5bfffb, 0xff2a2c36, "1_3", true, 0xff2a2c36).setInfo(null, "Psycho-Pass", "dominator");

		addShader("warbird", 7, Rarity.UNCOMMON, 0xff313640, 0xffd8d7d0, 0xffebac00, 0xffd8d7d0).setInfo(null, null, "warbird");
		addShader("matrix", 7, Rarity.RARE, 0xff053f3c, 0xffe1e1ff, 0xffd4ffff, 0xffffffff, "pipes", true, 0xff84ddd8).setInfo(null, null, "matrix");
		addShader("twili", 5, Rarity.EPIC, 0xff555d70, 0xff1a1e2b, 0xff222739, 0xff1db58e, "circuit", false, 0xff1db58e).setInfo(null, "The Legend of Zelda: Twilight Princess", "twili");
		addShader("usurper", 3, Rarity.EPIC, 0xff3e1e1e, 0xff5c6156, 0xff111010, 0xff737a6c, "circuit", false, 0xffca2f38).setInfo(null, "The Legend of Zelda: Twilight Princess", "usurper");
		entry = addShader("ancient", 6, Lib.RARITY_Masterwork, 0xff9c3a2d, 0xff514848, 0xfff6ae4a, 0xff80fcf2).setInfo(null, "The Legend of Zelda: Breath of the Wild", "ancient");
		addDynamicLayer(entry, "1_6", 0xaafaf307,
				(layer, superColour) -> ClientUtils.pulseRGBAlpha(0xff80fcf2, 60, .05f, .5f),
				(pre, partialTick) -> ClientUtils.toggleLightmap(pre, true));
		addLayer(entry, "circuit", 0x99bc9377);
		((ShaderCaseDrill)entry.getCase(new ResourceLocation("immersiveengineering", "drill"))).addHeadLayers(new ShaderLayer(new ResourceLocation("immersiveengineering", "items/drill_iron"), 0xff80fcf2));

		addShader("glacis", 6, Rarity.RARE, 0xff499bc2, 0x3376d0f9, 0x33bdfffd, 0x33bdfffd).setInfo(null, null, "glacis");
		addShader("phoenix", 5, Rarity.RARE, 0xff750000, 0xffd00000, 0xffff7f00, 0xffff7f00).setInfo(null, null, "phoenix");

		addShader("radiant", 3, Rarity.UNCOMMON, 0xffa36eab, 0xfff8dbbc, 0xffc79ab1, 0xffd8d8e3, "pipes", true, 0xfff1c91e).setInfo(null, "Kingdom Hearts", "radiant");
		addShader("hollow", 4, Rarity.UNCOMMON, 0xff542d1c, 0xffeec5e5, 0xffcc8980, 0xffc4a1aa, "pipes", true, 0xffc49838).setInfo(null, "Kingdom Hearts", "hollow");

		addShader("microshark", 8, Rarity.RARE, 0xff775054, 0xfff7f6cf, 0xff936267, 0xff936267, "shark", true, 0xffffffff).setInfo(null, "Terraria", "microshark");

		addShader("n7", 2, Rarity.EPIC, 0xff13171b, 0xff524d4a, 0xffe01919, 0xff8a8684, "whitestripe", false, 0xffffffff).setInfo(null, "Mass Effect", "n7");
		addShader("normandy", 8, Rarity.RARE, 0xffffffff, 0xff1a1a1a, 0xffffffff, 0xffffffff, "whitestripe", true, 0xff35447e).setInfo(null, "Mass Effect", "normandy");
		addShader("omnitool", 2, Rarity.RARE, 0x40ff952c, 0x30ff952c, 0x40ff952c, 0x20ff952c).setInfo(null, "Mass Effect", "omnitool");

		entry = addShader("kindled", 5, Rarity.EPIC, 0xff2b160b, 0xff3a3a3a, 0x80bf541f, 0xff4f4f4f).setInfo(null, "Dark Souls", "kindled");
		addBlockScaledLayer(entry, "minecraft:block/fire_layer_0", 0x80ffffff);

		entry = addShader("darkfire", 5, Rarity.EPIC, 0xff1e131b, 0xff211633, 0xff330812, 0xff412965).setInfo(null, "Kingdom Hearts", "darkfire");
		addBlockScaledLayer(entry, "immersiveengineering:block/shaders/greyscale_fire", 0xff9e83eb);

		entry = addShader("erruption", 5, Rarity.RARE, 0xff2b160b, 0xff58432f, 0x80bf301f, 0xff58432f).setInfo(null, null, "erruption");
		addBlockScaledLayer(entry, "minecraft:block/destroy_stage_8", 0xffff6314);

		addShader("waaagh", 5, Rarity.RARE, 0xff0f0f0f, 0xffdea712, 0xffc15b09, 0xff2f2f2f, "1_7", true, 0xff2f2f2f).setInfo(null, "Warhammer 40k", "waaagh");
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ItemStack item, ResourceLocation shaderType)
	{
		return ShaderRegistry.getShader(getShaderName(shader), shaderType);
	}

	public static ShaderRegistryEntry addShader(String name, int overlayType, Rarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade)
	{
		return addShader(name, overlayType, rarity, colourBackground, colourPrimary, colourSecondary, colourBlade, null, true, 0xffffffff);
	}

	public static ShaderRegistryEntry addShader(String name, int overlayType, Rarity rarity, int colourBackground, int colourPrimary, int colourSecondary, int colourBlade, String additionalTexture, boolean loot, int colourOverlay)
	{
		return ShaderRegistry.registerShader(new ResourceLocation("immersiveengineering", name), Integer.toString(overlayType), rarity, colourPrimary, colourSecondary, colourBackground, colourBlade, additionalTexture, colourOverlay, loot, true);
	}

	private static void addBlockScaledLayer(ShaderRegistryEntry entry, String texture, int colour)
	{
		entry.getCase(new ResourceLocation("immersiveengineering", "revolver")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0, 0, .25, .1875));
		entry.getCase(new ResourceLocation("immersiveengineering", "drill")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(10/64d, 34/64d, 26/64d, 50/64d).setCutoutBounds(.1875f, 0, .8125, .75f));
		entry.getCase(new ResourceLocation("immersiveengineering", "chemthrower")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(6/64d, 16/64d, 22/64d, 24/64d).setCutoutBounds(0, 0, 1, .5));
		entry.getCase(new ResourceLocation("immersiveengineering", "railgun")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(55/64d, 48/64d, 1, 58/64d).setCutoutBounds(.25, .125, .75, .6875));
		entry.getCase(new ResourceLocation("immersiveengineering", "shield")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0/32f, 9/32f, 14/32f, 26/32f).setCutoutBounds(.0625, 0, .9375, 1));
		entry.getCase(new ResourceLocation("immersiveengineering", "balloon")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(0, .375, .75, .875).setCutoutBounds(.125, 0, .875, .5));
		entry.getCase(new ResourceLocation("immersiveengineering", "banner")).addLayers(new ShaderLayer(new ResourceLocation(texture), colour).setTextureBounds(1/64d, 21/64d, 21/64d, 41/64d));
	}

	private static void addLayer(ShaderRegistryEntry entry, String texture, int colour)
	{
		entry.getCase(new ResourceLocation("immersiveengineering", "revolver")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_"+texture), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "drill")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_"+texture), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "chemthrower")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/chemthrower_"+texture), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "railgun")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/railgun_"+texture), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "shield")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/shield_"+texture), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "minecart")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_"+texture+".png"), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "balloon")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/balloon_"+texture), colour));
		entry.getCase(new ResourceLocation("immersiveengineering", "banner")).addLayers(new ShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/banner_"+texture), colour));
	}

	private static void addDynamicLayer(ShaderRegistryEntry entry, String texture, int colour, final BiFunction<ShaderLayer, Integer, Integer> func_getColour, final BiConsumer<Boolean, Float> func_modifyRender)
	{
		entry.getCase(new ResourceLocation("immersiveengineering", "revolver")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_"+texture), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "drill")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/drill_diesel_"+texture), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "chemthrower")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/chemthrower_"+texture), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "railgun")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/railgun_"+texture), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "shield")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:item/shaders/shield_"+texture), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "minecart")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:textures/models/shaders/minecart_"+texture+".png"), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "balloon")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/balloon_"+texture), colour, func_getColour, func_modifyRender));
		entry.getCase(new ResourceLocation("immersiveengineering", "banner")).addLayers(new InternalDynamicShaderLayer(new ResourceLocation("immersiveengineering:block/shaders/banner_"+texture), colour, func_getColour, func_modifyRender));
	}

	public void setDefaultTextureBounds(ResourceLocation rl, double... bounds)
	{
		ShaderRegistry.defaultLayerBounds.put(rl, bounds);
	}

	@Override
	public ResourceLocation getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			return new ResourceLocation(ItemNBTHelper.getString(stack, "shader_name"));
		return null;
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		ResourceLocation name = getShaderName(ctx.getItem());
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			BlockState blockState = world.getBlockState(pos);
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof BannerTileEntity)
			{
				ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation("immersiveengineering", "banner"));
				if(sCase!=null)
				{
					boolean wall = blockState.getBlock() instanceof WallBannerBlock;
					int orientation = wall?blockState.get(WallBannerBlock.HORIZONTAL_FACING).getIndex(): blockState.get(BannerBlock.ROTATION);
					world.setBlockState(pos, Cloth.shaderBanner.getDefaultState().with(IEProperties.FACING_HORIZONTAL, Direction.SOUTH));
					tile = world.getTileEntity(pos);
					if(tile instanceof ShaderBannerTileEntity)
					{
						//TODO use blockstate props similar to vanilla banners
						((ShaderBannerTileEntity)tile).wall = wall;
						((ShaderBannerTileEntity)tile).orientation = (byte)orientation;
						((ShaderBannerTileEntity)tile).shader.setShaderItem(Utils.copyStackWithAmount(ctx.getItem(), 1));
						tile.markDirty();
						return ActionResultType.SUCCESS;
					}
				}
			}
			else if(tile instanceof ShaderBannerTileEntity)
			{
				ItemStack current = ((ShaderBannerTileEntity)tile).shader.getShaderItem();
				if(!current.isEmpty() && !world.isRemote && (ctx.getPlayer()==null || !ctx.getPlayer().abilities.isCreativeMode))
				{
					double dx = pos.getX()+.5+ctx.getFace().getXOffset();
					double dy = pos.getY()+.5+ctx.getFace().getYOffset();
					double dz = pos.getZ()+.5+ctx.getFace().getZOffset();
					ItemEntity entityitem = new ItemEntity(world, dx, dy, dz, current.copy());
					entityitem.setDefaultPickupDelay();
					world.addEntity(entityitem);
				}
				((ShaderBannerTileEntity)tile).shader.setShaderItem(Utils.copyStackWithAmount(ctx.getItem(), 1));
				tile.markDirty();
				return ActionResultType.SUCCESS;
			}

		}
		return ActionResultType.FAIL;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		//TODO proper translation
		list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader.level")
				.appendText(this.getRarity(stack).color.toString())
				.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"shader.rarity."+this.getRarity(stack).name().toLowerCase(Locale.US)))
		);
		if(!Screen.hasShiftDown())
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader.applyTo")
					.appendText(" ")
					.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"holdShift")));
		else
		{
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader.applyTo"));
			ResourceLocation rl = getShaderName(stack);
			if(rl!=null)
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(rl).getCases();
				for(ShaderCase sCase : array)
					if(!(sCase instanceof ShaderCaseItem))
						list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader."+sCase.getShaderType())
								.setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
			}
		}
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName(@Nonnull ItemStack stack)
	{
		ITextComponent itc = super.getDisplayName(stack);
		ResourceLocation rl = getShaderName(stack);
		if(rl!=null)
			itc.appendText(": ").appendSibling(new TranslationTextComponent("item."+rl.getNamespace()+".shader.name."+rl.getPath()));
		return itc;
	}

	@Nonnull
	@Override
	public Rarity getRarity(ItemStack stack)
	{
		ResourceLocation rl = getShaderName(stack);
		return ShaderRegistry.shaderRegistry.containsKey(rl)?ShaderRegistry.shaderRegistry.get(rl).getRarity(): Rarity.COMMON;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> list)
	{
		if(this.isInGroup(tab))
			for(ResourceLocation key : ShaderRegistry.shaderRegistry.keySet())
			{
				ItemStack s = new ItemStack(this);
				ItemNBTHelper.putString(s, "shader_name", key.toString());
				list.add(s);
			}
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		ResourceLocation name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation("immersiveengineering", "item"));
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				if(pass < layers.length&&layers[pass]!=null)
					return layers[pass].getColour();
				return 0xffffffff;
			}
		}
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	public String getModelCacheKey(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			return ItemNBTHelper.getString(stack, "shader_name");
		return null;
	}

	@Override
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		ResourceLocation name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation("immersiveengineering", "item"));
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				ArrayList<ResourceLocation> list = new ArrayList<>(layers.length);
				for(ShaderLayer layer : layers)
					list.add(layer.getTexture());
				return list;
			}
		}
//		return Arrays.asList(new ResourceLocation("immersiveengineering:item/shader_0"));
		return Arrays.asList(new ResourceLocation("immersiveengineering:item/shader_0"), new ResourceLocation("immersiveengineering:item/shader_1"), new ResourceLocation("immersiveengineering:item/shader_2"));
	}

	private static class InternalDynamicShaderLayer extends DynamicShaderLayer
	{
		private final BiFunction<ShaderLayer, Integer, Integer> func_getColour;
		private final BiConsumer<Boolean, Float> func_modifyRender;

		public InternalDynamicShaderLayer(ResourceLocation texture, int colour, BiFunction<ShaderLayer, Integer, Integer> func_getColour, BiConsumer<Boolean, Float> func_modifyRender)
		{
			super(texture, colour);
			this.func_getColour = func_getColour;
			this.func_modifyRender = func_modifyRender;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public int getColour()
		{
			if(func_getColour!=null)
				return func_getColour.apply(this, super.getColour());
			return super.getColour();
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void modifyRender(boolean pre, float partialTick)
		{
			if(func_modifyRender!=null)
				func_modifyRender.accept(pre, partialTick);
		}
	}
}