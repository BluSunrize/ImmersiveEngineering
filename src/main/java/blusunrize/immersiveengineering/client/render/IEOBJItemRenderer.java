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
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEOBJItemRenderer extends ItemStackTileEntityRenderer
{
	public static final ItemStackTileEntityRenderer INSTANCE = new IEOBJItemRenderer();
	private static FloatBuffer transform = GLAllocation.createDirectFloatBuffer(16);

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
				List<BakedQuad> quads = new ArrayList<>();
				for(String[] groups : callback.getSpecialGroups(stack, transformType, IESmartObjModel.tempEntityStatic))
				{
					TransformationMatrix mat = callback.getTransformForGroups(stack, groups, transformType, mc().player,
							partialTicks);
					mat.push(matrixStackIn);
					boolean bright = callback.areGroupsFullbright(stack, groups);
					IVertexBuilder builder;
					if(bright)
						builder = bufferIn.getBuffer(IERenderTypes.SOLID_FULLBRIGHT);
					else
						builder = bufferIn.getBuffer(RenderType.getSolid());
					renderQuadsForGroups(groups, callback, obj, quads, stack,
							sCase, shader, true, matrixStackIn, builder, visible, partialTicks,
							combinedLightIn, combinedOverlayIn);
					matrixStackIn.pop();
				}
				renderQuadsForGroups(visible.toArray(new String[0]), callback, obj, quads, stack,
						sCase, shader, false, matrixStackIn, bufferIn.getBuffer(RenderType.getSolid()),
						visible, partialTicks, combinedLightIn, combinedOverlayIn);
			}
		}
	}

	private void renderQuadsForGroups(String[] groups, IOBJModelCallback<ItemStack> callback, IESmartObjModel model,
									  List<BakedQuad> quadsForGroup, ItemStack stack, ShaderCase sCase, ItemStack shader,
									  boolean dynamic, MatrixStack matrix, IVertexBuilder builder, Set<String> visible,
									  float partialTicks, int light, int overlay)
	{
		quadsForGroup.clear();
		for(String g : groups)
		{
			if(visible.contains(g)&&callback.shouldRenderGroup(stack, g))
				quadsForGroup.addAll(model.addQuadsForGroup(callback, stack, g, sCase, !dynamic)
						.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			visible.remove(g);
		}
		VertexBufferConsumer vbc = new VertexBufferConsumer(builder);
		ShaderLayer lastShaderLayer = null;
		for(BakedQuad bq : quadsForGroup)
		{
			//Switch to or between dynamic layers
			/*TODO move to RenderType and MatrixStack
			boolean switchDynamic = layer!=lastShaderLayer;
			if(switchDynamic)
			{
				//interrupt batch
				tes.draw();

				if(lastShaderLayer!=null)//finish dynamic call on last layer
					lastShaderLayer.modifyRender(false, partialTicks);

				//set new layer
				lastShaderLayer = layer;

				if(lastShaderLayer!=null)//start dynamic call on layer
					lastShaderLayer.modifyRender(true, partialTicks);
				//start new batch
				if(!callback.areGroupsFullbright(stack, groups))
					bb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				else
					bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			}
			 */
			builder.addQuad(matrix.getLast(), bq, 1, 1, 1, light, overlay);
		}
		if(lastShaderLayer!=null)//finish dynamic call on final layer
			lastShaderLayer.modifyRender(false, partialTicks);

	}
}
