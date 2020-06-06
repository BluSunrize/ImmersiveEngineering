/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.IEShaderLayerCompositeTexture;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerStandingBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerWallBlock;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.model.BannerModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ShaderBannerRenderer extends TileEntityRenderer<ShaderBannerTileEntity>
{
	private final BannerModel bannerModel = new BannerModel();

	@Override
	public void render(ShaderBannerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		long time = te.getWorldNonnull().getGameTime();
		GlStateManager.pushMatrix();
		if(!te.wall)
		{
			int orientation = te.getState().get(ShaderBannerStandingBlock.ROTATION);
			GlStateManager.translated((float)x+0.5F, (float)y+0.5F, (float)z+0.5F);
			float f1 = (float)(orientation*360)/16.0F;
			GlStateManager.rotatef(-f1, 0.0F, 1.0F, 0.0F);
			this.bannerModel.func_205057_b().showModel = true;
		}
		else
		{
			Direction facing = te.getState().get(ShaderBannerWallBlock.FACING);
			float rotation = facing.getHorizontalAngle();

			GlStateManager.translated((float)x+0.5F, (float)y-0.16666667F, (float)z+0.5F);
			GlStateManager.rotatef(-rotation, 0.0F, 1.0F, 0.0F);
			GlStateManager.translated(0.0F, -0.3125F, -0.4375F);
			this.bannerModel.func_205057_b().showModel = false;
		}

		BlockPos blockpos = te.getPos();
		float f3 = (float)(blockpos.getX()*7+blockpos.getY()*9+blockpos.getZ()*13)+(float)time+partialTicks;
		this.bannerModel.func_205056_c().rotateAngleX = (-0.0125F+0.01F*MathHelper.cos(f3*(float)Math.PI*0.02F))*(float)Math.PI;
		GlStateManager.enableRescaleNormal();
		ResourceLocation resourcelocation = this.getBannerResourceLocation(te);

		if(resourcelocation!=null)
		{
			this.bindTexture(resourcelocation);
			GlStateManager.pushMatrix();

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableAlphaTest();
			GlStateManager.enableBlend();

			GlStateManager.scalef(0.6666667F, -0.6666667F, -0.6666667F);
			this.bannerModel.renderBanner();

			GlStateManager.disableBlend();
			GlStateManager.disableAlphaTest();

			GlStateManager.popMatrix();
		}

		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("textures/entity/banner_base.png");
	private static final HashMap<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();

	@Nullable
	private ResourceLocation getBannerResourceLocation(ShaderBannerTileEntity bannerObj)
	{
		ResourceLocation name = null;
		ShaderCase sCase = null;
		ItemStack shader = bannerObj.shader.getShaderItem();
		if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
		{
			IShaderItem iShaderItem = ((IShaderItem)shader.getItem());
			name = iShaderItem.getShaderName(shader);
			if(CACHE.containsKey(name))
				return CACHE.get(name);
			sCase = iShaderItem.getShaderCase(shader, null, bannerObj.shader.getShaderType());
		}

		if(sCase!=null)
		{
			ShaderLayer[] layers = sCase.getLayers();
			ResourceLocation textureLocation = new ResourceLocation(name.getNamespace(), "bannershader/"+name.getPath());
			ClientUtils.mc().getTextureManager().loadTexture(textureLocation, new IEShaderLayerCompositeTexture(BASE_TEXTURE, layers));
			CACHE.put(name, textureLocation);
			return textureLocation;
		}
		return null;
	}
}
