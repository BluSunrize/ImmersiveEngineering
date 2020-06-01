/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel.ShadedQuads;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEOBJItemRenderer extends ItemStackTileEntityRenderer
{
	public static final ItemStackTileEntityRenderer INSTANCE = new IEOBJItemRenderer();

	@Override
	public void render(ItemStack stack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn,
					   int combinedLightIn, int combinedOverlayIn)
	{
		float partialTicks = mc().getRenderPartialTicks();
		if(stack.getItem() instanceof IOBJModelCallback)
		{
			IOBJModelCallback<ItemStack> callback = (IOBJModelCallback<ItemStack>)stack.getItem();
			World w = IESmartObjModel.tempEntityStatic!=null?IESmartObjModel.tempEntityStatic.world: null;
			IBakedModel model = mc().getItemRenderer().getItemModelWithOverrides(stack, w,
					IESmartObjModel.tempEntityStatic);
			if(model instanceof IESmartObjModel)
			{

				ItemStack shader;
				ShaderCase sCase;
				{
					Pair<ItemStack, ShaderCase> tmp = stack.getCapability(CapabilityShader.SHADER_CAPABILITY)
							.map(wrapper ->
							{
								ItemStack shaderInner = wrapper.getShaderItem();
								ShaderCase sCaseInner = null;
								if(!shaderInner.isEmpty()&&shaderInner.getItem() instanceof IShaderItem)
									sCaseInner = ((IShaderItem)shaderInner.getItem()).getShaderCase(shaderInner, stack, wrapper.getShaderType());
								return new ImmutablePair<>(shaderInner, sCaseInner);
							})
							.orElse(new ImmutablePair<>(ItemStack.EMPTY, null));
					shader = tmp.getLeft();
					sCase = tmp.getRight();
				}
				IESmartObjModel obj = (IESmartObjModel)model;
				Set<String> visible = new HashSet<>();
				for(String g : OBJHelper.getGroups(obj.baseModel).keySet())
					if(callback.shouldRenderGroup(stack, g))
						visible.add(g);
				TransformType transformType = obj.lastCameraTransform;
				for(String[] groups : callback.getSpecialGroups(stack, transformType, IESmartObjModel.tempEntityStatic))
				{
					TransformationMatrix mat = callback.getTransformForGroups(stack, groups, transformType, mc().player,
							partialTicks);
					mat.push(matrixStackIn);
					renderQuadsForGroups(groups, callback, obj, stack, sCase, matrixStackIn, bufferIn, visible,
							combinedLightIn, combinedOverlayIn);
					matrixStackIn.pop();
				}
				renderQuadsForGroups(visible.toArray(new String[0]), callback, obj, stack,
						sCase, matrixStackIn, bufferIn, visible, combinedLightIn, combinedOverlayIn);
			}
		}
	}

	private void renderQuadsForGroups(String[] groups, IOBJModelCallback<ItemStack> callback, IESmartObjModel model,
									  ItemStack stack, ShaderCase sCase, MatrixStack matrix, IRenderTypeBuffer buffer,
									  Set<String> visible, int light, int overlay)
	{
		List<ShadedQuads> quadsByLayer = new ArrayList<>();
		for(String g : groups)
		{
			if(visible.contains(g)&&callback.shouldRenderGroup(stack, g))
				quadsByLayer.addAll(model.addQuadsForGroup(callback, stack, g, sCase, true)
						.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			visible.remove(g);
		}
		matrix.push();
		for(ShadedQuads quadsForLayer : quadsByLayer)
		{
			boolean bright = callback.areGroupsFullbright(stack, groups);
			Function<ResourceLocation, RenderType> toBaseType;
			if(bright)
				toBaseType = IERenderTypes::getFullbrightTranslucent;
			else if(quadsForLayer.layer.isTranslucent())
				toBaseType = RenderType::getEntityTranslucent;
			else
				toBaseType = RenderType::getEntityCutout;
			IVertexBuilder builder = buffer.getBuffer(quadsForLayer.layer.getRenderType(toBaseType));
			Vector4f color = quadsForLayer.layer.getColor();
			for(BakedQuad quad : quadsForLayer.quadsInLayer)
				addQuadWithAlpha(
						matrix.getLast(), quad, color, new int[]{light, light, light, light}, overlay, builder
				);
			matrix.scale(1.01F, 1.01F, 1.01F);
		}
		matrix.pop();
	}

	private void addQuadWithAlpha(MatrixStack.Entry matrixEntryIn, BakedQuad quadIn, Vector4f color,
								  int[] combinedLightsIn, int combinedOverlayIn, IVertexBuilder builder)
	{
		int[] aint = quadIn.getVertexData();
		Vec3i normalInt = quadIn.getFace().getDirectionVec();
		Vector3f normal = new Vector3f(normalInt.getX(), normalInt.getY(), normalInt.getZ());
		Matrix4f transform = matrixEntryIn.getMatrix();
		normal.transform(matrixEntryIn.getNormal());
		int i = 8;
		int j = aint.length/8;

		try(MemoryStack memorystack = MemoryStack.stackPush())
		{
			ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getSize());
			IntBuffer intbuffer = bytebuffer.asIntBuffer();

			for(int k = 0; k < j; ++k)
			{
				intbuffer.clear();
				intbuffer.put(aint, k*8, 8);
				//TODO general formats?
				float x = bytebuffer.getFloat(0);
				float y = bytebuffer.getFloat(4);
				float z = bytebuffer.getFloat(8);
				int lightmapUV = builder.applyBakedLighting(combinedLightsIn[k], bytebuffer);
				float texU = bytebuffer.getFloat(16);
				float texV = bytebuffer.getFloat(20);
				Vector4f vector4f = new Vector4f(x, y, z, 1.0F);
				vector4f.transform(transform);
				builder.applyBakedNormals(normal, bytebuffer, matrixEntryIn.getNormal());
				builder.addVertex(
						vector4f.getX(), vector4f.getY(), vector4f.getZ(),
						color.getX(), color.getY(), color.getZ(), color.getW(),
						texU, texV, combinedOverlayIn, lightmapUV,
						normal.getX(), normal.getY(), normal.getZ()
				);
			}
		}
	}
}
