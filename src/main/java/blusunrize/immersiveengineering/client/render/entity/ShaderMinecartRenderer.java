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
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.model.MinecartModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class ShaderMinecartRenderer<T extends AbstractMinecartEntity> extends MinecartRenderer<T>
{
	public static Int2ObjectMap<ItemStack> shadedCarts = new Int2ObjectOpenHashMap<>();
	public static boolean rendersReplaced = false;

	private final MinecartRenderer<T> baseRenderer;

	public ShaderMinecartRenderer(MinecartRenderer<T> base, EntityRendererManager manager)
	{
		super(manager);
		this.baseRenderer = base;
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
	{
		ShaderCase sCase = null;
		ItemStack shader;
		if(shadedCarts.containsKey(entity.getEntityId()))
		{
			shader = shadedCarts.get(entity.getEntityId());
			if(shader!=null&&!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, null, new ResourceLocation(ImmersiveEngineering.MODID, "minecart"));
		}
		baseRenderer.render(entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
		if(sCase!=null)
		{
			matrixStackIn.push();
			applyTransforms(matrixStackIn, entity, partialTicks, entityYaw);
			MinecartModel<?> model = getModel();
			List<ModelRenderer> boxList = Lists.newArrayList(model.getParts());
			boxList.get(5).rotationPointY = 4.1F;
			for(int part = 0; part < boxList.size()-1; part++)
				if(boxList.get(part)!=null)
				{
					float scale = 1;
					ShaderLayer[] layers = sCase.getLayers();

					//identify part 1+2, they shouldn'T render with additional?!

					for(int pass = 0; pass < layers.length; pass++)
						if(sCase.shouldRenderGroupForPass(""+part, pass))
						{
							Vector4f col = sCase.getRenderColor(""+part, pass, new net.minecraft.client.renderer.Vector4f(1, 1, 1, 1));
							matrixStackIn.push();
							matrixStackIn.scale(scale, scale, scale);

							RenderType type = sCase.getLayers()[pass].getRenderType(RenderType.getEntityTranslucent(
									//TODO should be atlas?
									sCase.getTextureReplacement(Integer.toString(part), pass).getTextureLocation()
							));

							ModelRenderer subModel = boxList.get(part);
							boolean oldMirrored = subModel.mirror;
							subModel.mirror = ((ShaderCaseMinecart)sCase).mirrorSideForPass[pass];
							subModel.render(matrixStackIn, bufferIn.getBuffer(type), packedLightIn,
									OverlayTexture.NO_OVERLAY, col.getX(), col.getY(), col.getZ(), col.getW());
							subModel.mirror = oldMirrored;

							matrixStackIn.pop();
						}
				}
			matrixStackIn.pop();
		}
	}

	private MinecartModel<?> getModel()
	{
		if(baseRenderer.modelMinecart instanceof MinecartModel<?>)
			return (MinecartModel<?>)baseRenderer.modelMinecart;
		else
			return new MinecartModel<>();
	}

	private void applyTransforms(MatrixStack matrixStackIn, T entityIn, float partialTicks, float entityYaw)
	{
		long i = (long)entityIn.getEntityId()*493286711L;
		i = i*i*4392167121L+i*98761L;
		float f = (((float)(i >> 16&7L)+0.5F)/8.0F-0.5F)*0.004F;
		float f1 = (((float)(i >> 20&7L)+0.5F)/8.0F-0.5F)*0.004F;
		float f2 = (((float)(i >> 24&7L)+0.5F)/8.0F-0.5F)*0.004F;
		matrixStackIn.translate(f, f1, f2);
		double d0 = MathHelper.lerp(partialTicks, entityIn.lastTickPosX, entityIn.getPosX());
		double d1 = MathHelper.lerp(partialTicks, entityIn.lastTickPosY, entityIn.getPosY());
		double d2 = MathHelper.lerp(partialTicks, entityIn.lastTickPosZ, entityIn.getPosZ());
		Vec3d vec3d = entityIn.getPos(d0, d1, d2);
		float f3 = MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch);
		if(vec3d!=null)
		{
			Vec3d vec3d1 = entityIn.getPosOffset(d0, d1, d2, 0.3F);
			Vec3d vec3d2 = entityIn.getPosOffset(d0, d1, d2, -0.3F);
			if(vec3d1==null)
				vec3d1 = vec3d;

			if(vec3d2==null)
				vec3d2 = vec3d;

			matrixStackIn.translate(vec3d.x-d0, (vec3d1.y+vec3d2.y)/2.0D-d1, vec3d.z-d2);
			Vec3d vec3d3 = vec3d2.add(-vec3d1.x, -vec3d1.y, -vec3d1.z);
			if(vec3d3.length()!=0.0D)
			{
				vec3d3 = vec3d3.normalize();
				entityYaw = (float)(Math.atan2(vec3d3.z, vec3d3.x)*180.0D/Math.PI);
				f3 = (float)(Math.atan(vec3d3.y)*73.0D);
			}
		}

		matrixStackIn.translate(0.0D, 0.375D, 0.0D);
		matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F-entityYaw));
		matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-f3));
		float f5 = (float)entityIn.getRollingAmplitude()-partialTicks;
		float f6 = entityIn.getDamage()-partialTicks;
		if(f6 < 0.0F)
			f6 = 0.0F;

		if(f5 > 0.0F)
			matrixStackIn.rotate(Vector3f.XP.rotationDegrees(MathHelper.sin(f5)*f5*f6/10.0F*(float)entityIn.getRollingDirection()));
		matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
	}

	public static <T extends Entity> void overrideModelIfMinecart(EntityType<T> type)
	{
		EntityRendererManager rendererManager = Minecraft.getInstance().getRenderManager();
		EntityRenderer<T> render = (EntityRenderer<T>)rendererManager.renderers.get(type);
		if(render instanceof MinecartRenderer<?>)
			rendererManager.register(
					type,
					// Raw types to work around generics issues
					new ShaderMinecartRenderer((MinecartRenderer<?>)render, rendererManager)
			);
	}
}