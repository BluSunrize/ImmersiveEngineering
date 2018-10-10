/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.cloth.TileEntityShaderBanner;
import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.HashMap;

public class TileRenderShaderBanner extends TileEntitySpecialRenderer<TileEntityShaderBanner>
{
	private final ModelBanner bannerModel = new ModelBanner();

	@Override
	public void render(TileEntityShaderBanner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		boolean flag = te.getWorld()!=null;
		int orientation = flag?te.orientation: 0;
		long time = flag?te.getWorld().getTotalWorldTime(): 0L;
		GlStateManager.pushMatrix();
		float f = 0.6666667F;

		if(!te.wall)
		{
			GlStateManager.translate((float)x+0.5F, (float)y+0.5F, (float)z+0.5F);
			float f1 = (float)(orientation*360)/16.0F;
			GlStateManager.rotate(-f1, 0.0F, 1.0F, 0.0F);
			this.bannerModel.bannerStand.showModel = true;
		}
		else
		{
			float rotation = orientation==2?180: orientation==3?0: orientation==4?90: -90;

			GlStateManager.translate((float)x+0.5F, (float)y-0.16666667F, (float)z+0.5F);
			GlStateManager.rotate(-rotation, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
			this.bannerModel.bannerStand.showModel = false;
		}

		BlockPos blockpos = te.getPos();
		float f3 = (float)(blockpos.getX()*7+blockpos.getY()*9+blockpos.getZ()*13)+(float)time+partialTicks;
		this.bannerModel.bannerSlate.rotateAngleX = (-0.0125F+0.01F*MathHelper.cos(f3*(float)Math.PI*0.02F))*(float)Math.PI;
		GlStateManager.enableRescaleNormal();
		ResourceLocation resourcelocation = this.getBannerResourceLocation(te);

		if(resourcelocation!=null)
		{
			this.bindTexture(resourcelocation);
			GlStateManager.pushMatrix();

			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();

			GlStateManager.scale(0.6666667F, -0.6666667F, -0.6666667F);
			this.bannerModel.renderBanner();

			GlStateManager.disableBlend();
			GlStateManager.disableAlpha();

			GlStateManager.popMatrix();
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		GlStateManager.popMatrix();
	}

	private static final ResourceLocation BASE_TEXTURE = new ResourceLocation("textures/entity/banner_base.png");
	private static final HashMap<String, ResourceLocation> CACHE = new HashMap<>();

	@Nullable
	private ResourceLocation getBannerResourceLocation(TileEntityShaderBanner bannerObj)
	{
		String name = null;
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
			ResourceLocation textureLocation = new ResourceLocation("immersiveengineering", "bannershader/"+name);
			ClientUtils.mc().getTextureManager().loadTexture(textureLocation, new IEShaderLayerCompositeTexture(BASE_TEXTURE, layers));
			CACHE.put(name, textureLocation);
			return textureLocation;
		}
		return null;
	}
}
