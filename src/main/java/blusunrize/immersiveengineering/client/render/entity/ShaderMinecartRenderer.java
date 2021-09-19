/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.impl.ShaderCaseMinecart;
import blusunrize.immersiveengineering.mixin.accessors.client.MinecartRendererAccess;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ShaderMinecartRenderer<T extends AbstractMinecart> extends MinecartRenderer<T>
{
	public static Int2ObjectMap<ItemStack> shadedCarts = new Int2ObjectOpenHashMap<>();
	public static boolean rendersReplaced = false;

	private final MinecartRenderer<T> baseRenderer;

	public ShaderMinecartRenderer(MinecartRenderer<T> base, EntityRenderDispatcher manager)
	{
		super(manager);
		this.baseRenderer = base;
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn)
	{
		ShaderCase sCase = null;
		ItemStack shader;
		if(shadedCarts.containsKey(entity.getId()))
		{
			shader = shadedCarts.get(entity.getId());
			if(shader!=null&&!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, null, new ResourceLocation(ImmersiveEngineering.MODID, "minecart"));
		}
		baseRenderer.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		if(sCase!=null)
		{
			matrixStackIn.pushPose();
			applyTransforms(matrixStackIn, entity, partialTicks, entityYaw);
			MinecartModel<?> model = getModel();
			List<ModelPart> boxList = Lists.newArrayList(model.parts());
			boxList.get(5).y = 4.1F;
			for(int part = 0; part < boxList.size()-1; part++)
				if(boxList.get(part)!=null)
				{
					float scale = 1;
					ShaderLayer[] layers = sCase.getLayers();

					//identify part 1+2, they shouldn'T render with additional?!

					for(int pass = 0; pass < layers.length; pass++)
						if(sCase.shouldRenderGroupForPass(""+part, pass))
						{
							Vector4f col = sCase.getRenderColor(""+part, pass, new Vector4f(1, 1, 1, 1));
							matrixStackIn.pushPose();
							matrixStackIn.scale(scale, scale, scale);

							RenderType type = sCase.getLayers()[pass].getRenderType(RenderType.entityTranslucent(
									sCase.getTextureReplacement(Integer.toString(part), pass)
							));

							ModelPart subModel = boxList.get(part);
							boolean oldMirrored = subModel.mirror;
							subModel.mirror = ((ShaderCaseMinecart)sCase).mirrorSideForPass[pass];
							subModel.render(matrixStackIn, bufferIn.getBuffer(type), packedLightIn,
									OverlayTexture.NO_OVERLAY, col.x(), col.y(), col.z(), col.w());
							subModel.mirror = oldMirrored;

							matrixStackIn.popPose();
						}
				}
			matrixStackIn.popPose();
		}
	}

	private MinecartModel<?> getModel()
	{
		EntityModel<?> model = ((MinecartRendererAccess)baseRenderer).getModel();
		if(model instanceof MinecartModel<?>)
			return (MinecartModel<?>)model;
		else
			return new MinecartModel<>();
	}

	private void applyTransforms(PoseStack matrixStackIn, T entityIn, float partialTicks, float entityYaw)
	{
		long i = (long)entityIn.getId()*493286711L;
		i = i*i*4392167121L+i*98761L;
		float f = (((float)(i >> 16&7L)+0.5F)/8.0F-0.5F)*0.004F;
		float f1 = (((float)(i >> 20&7L)+0.5F)/8.0F-0.5F)*0.004F;
		float f2 = (((float)(i >> 24&7L)+0.5F)/8.0F-0.5F)*0.004F;
		matrixStackIn.translate(f, f1, f2);
		double d0 = Mth.lerp(partialTicks, entityIn.xOld, entityIn.getX());
		double d1 = Mth.lerp(partialTicks, entityIn.yOld, entityIn.getY());
		double d2 = Mth.lerp(partialTicks, entityIn.zOld, entityIn.getZ());
		Vec3 vec3d = entityIn.getPos(d0, d1, d2);
		float f3 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);
		if(vec3d!=null)
		{
			Vec3 vec3d1 = entityIn.getPosOffs(d0, d1, d2, 0.3F);
			Vec3 vec3d2 = entityIn.getPosOffs(d0, d1, d2, -0.3F);
			if(vec3d1==null)
				vec3d1 = vec3d;

			if(vec3d2==null)
				vec3d2 = vec3d;

			matrixStackIn.translate(vec3d.x-d0, (vec3d1.y+vec3d2.y)/2.0D-d1, vec3d.z-d2);
			Vec3 vec3d3 = vec3d2.add(-vec3d1.x, -vec3d1.y, -vec3d1.z);
			if(vec3d3.length()!=0.0D)
			{
				vec3d3 = vec3d3.normalize();
				entityYaw = (float)(Math.atan2(vec3d3.z, vec3d3.x)*180.0D/Math.PI);
				f3 = (float)(Math.atan(vec3d3.y)*73.0D);
			}
		}

		matrixStackIn.translate(0.0D, 0.375D, 0.0D);
		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F-entityYaw));
		matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(-f3));
		float f5 = (float)entityIn.getHurtTime()-partialTicks;
		float f6 = entityIn.getDamage()-partialTicks;
		if(f6 < 0.0F)
			f6 = 0.0F;

		if(f5 > 0.0F)
			matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(f5)*f5*f6/10.0F*(float)entityIn.getHurtDir()));
		matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
	}

	public static <T extends Entity> void overrideModelIfMinecart(EntityType<T> type)
	{
		EntityRenderDispatcher rendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
		EntityRenderer<T> render = (EntityRenderer<T>)rendererManager.renderers.get(type);
		if(render instanceof MinecartRenderer<?>)
			rendererManager.register(
					type,
					// Raw types to work around generics issues
					new ShaderMinecartRenderer((MinecartRenderer<?>)render, rendererManager)
			);
	}
}