/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.entity.IEModelLayers;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class ModelPowerpack<T extends LivingEntity> extends ModelIEArmorBase<T>
{
	private final ModelPart meterNeedle;

	public ModelPowerpack(ModelPart part)
	{
		super(part, RenderType::entityTranslucent);
		this.meterNeedle = part.getChild("body").getChild("meterNeedle");
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		if(entityTemp!=null)
		{
			ItemStack chest = entityTemp.getItemBySlot(EquipmentSlot.CHEST);
			ItemStack powerpack = null;
			float storage = 0;
			if(!chest.isEmpty()&&chest.getItem() instanceof PowerpackItem)
				powerpack = chest;
			else if(!chest.isEmpty()&&chest.getItem() instanceof ArmorItem&&ItemNBTHelper.hasKey(chest, "IE:Powerpack"))
				powerpack = ItemNBTHelper.getItemStack(chest, "IE:Powerpack");
			else if(IEBipedLayerRenderer.POWERPACK_PLAYERS.containsKey(entityTemp.getUUID()))
				powerpack = IEBipedLayerRenderer.POWERPACK_PLAYERS.get(entityTemp.getUUID()).getLeft();

			if(powerpack!=null)
			{
				float max = EnergyHelper.getMaxEnergyStored(powerpack);
				storage = max <= 0?0: EnergyHelper.getEnergyStored(powerpack)/max;
				meterNeedle.zRot = 0.5235987f-(1.047197f*storage);
			}
		}

		super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

		if(entityTemp!=null)
		{
			TextureAtlasSprite wireTexture = Minecraft.getInstance()
					.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
					.apply(new ResourceLocation(Lib.MODID, "textures/block/wire"));
			for(InteractionHand hand : InteractionHand.values())
			{
				ItemStack stack = entityTemp.getItemInHand(hand);
				if(!stack.isEmpty()&&EnergyHelper.isFluxRelated(stack))
				{
					boolean right = (hand==InteractionHand.MAIN_HAND)==(entityTemp.getMainArm()==HumanoidArm.RIGHT);
					float angleX = (right?rightArm: leftArm).xRot;
					float angleZ = (right?rightArm: leftArm).zRot;
					String cacheKey = keyFormat.format(angleX)+"_"+keyFormat.format(angleZ);
					Vec3[] vex;
					try
					{
						vex = (right?catenaryCacheRight: catenaryCacheLeft).get(cacheKey, () ->
						{
							double armLength = .75f;
							double x = .3125+(right?1: -1)*armLength*Math.sin(angleZ);
							double y = armLength*Math.cos(angleX);
							double z = armLength*Math.sin(angleX);

							return WireUtils.getConnectionCatenary(new Vec3(.484375, -.75, .25), new Vec3(x, -y, z), 1.5);
						});
					} catch(Exception e)
					{
						throw new RuntimeException(e);
					}

					float vStep = 1f/vex.length;

					TransformingVertexBuilder builder = new TransformingVertexBuilder(bufferIn, matrixStackIn, DefaultVertexFormat.NEW_ENTITY);
					double scaleX = right?-1: 1;
					builder.defaultColor(.93f, .63f, .27f, 1);
					builder.setLight(packedLightIn);
					builder.setOverlay(packedOverlayIn);
					final float v0 = wireTexture.getV0();
					final float v1 = wireTexture.getV1();
					for(int i = 1; i < vex.length; i++)
						for(int offset = 0; offset < 2; ++offset)
						{
							int iHere = i-offset;
							int iThere = i-1+offset;
							Vec3 vecHere = vex[iHere];
							Vec3 vecThere = vex[iThere];
							builder.setNormal((float)(vecThere.z-vecHere.z), 0, (float)(vecHere.x-vecThere.x));
							for(int index : new int[]{iHere, iThere})
							{
								Vec3 vec = vex[index];
								double xA = scaleX*vec.x-.015625;
								double xB = scaleX*vec.x+.015625;
								if(index==iHere)
								{
									double tmp = xA;
									xA = xB;
									xB = tmp;
								}
								builder.vertex(xA, -vec.y, vec.z)
										.uv(wireTexture.getU(vStep*index), v0)
										.endVertex();
								builder.vertex(xB, -vec.y, vec.z)
										.uv(wireTexture.getU(vStep*index), v1)
										.endVertex();
							}
							builder.setNormal((float)(vecThere.y-vecHere.y), (float)(vecHere.x-vecThere.x), 0);
							for(int index : new int[]{iHere, iThere})
							{
								Vec3 vec = vex[index];
								double yA = -vec.y-.015625;
								double yB = -vec.y;
								if(index==iThere)
								{
									double tmp = yA;
									yA = yB;
									yB = tmp;
								}
								builder.vertex(scaleX*vec.x, yA, vec.z)
										.uv(wireTexture.getU(vStep*index), v0)
										.endVertex();
								builder.vertex(scaleX*vec.x, yB, vec.z)
										.uv(wireTexture.getU(vStep*index), v1)
										.endVertex();
							}
						}
				}
			}
		}
	}

	static final DecimalFormat keyFormat = new DecimalFormat("0.0000");
	public static final Cache<String, Vec3[]> catenaryCacheLeft = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();
	public static final Cache<String, Vec3[]> catenaryCacheRight = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();

	static ModelPowerpack<?> modelInstance;

	public static <T extends LivingEntity>
	ModelPowerpack<T> getModel(EntityModelSet modelSet)
	{
		if(modelInstance==null)
			modelInstance = new ModelPowerpack<>(modelSet.bakeLayer(IEModelLayers.POWERPACK));
		return (ModelPowerpack<T>)modelInstance;
	}

	public static LayerDefinition createLayers()
	{
		MeshDefinition data = new MeshDefinition();
		PartDefinition root = data.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

		// TODO better names!
		body.addOrReplaceChild(
				"part0",
				CubeListBuilder.create().texOffs(40, 0).addBox(-4f, -5f, -2f, 8, 10, 3),
				PartPose.offset(0, 5, 4)
		);
		PartDefinition part1 = body.addOrReplaceChild(
				"part1",
				CubeListBuilder.create().texOffs(12, 0).addBox(-3f, -2f, -2f, 6, 4, 4),
				PartPose.offset(0, 12, 4f)
		);
		body.addOrReplaceChild(
				"part2",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1f, -4f, -1f, 2, 8, 2),
				PartPose.offset(-5f, 5, 3f)
		);
		body.addOrReplaceChild(
				"part3",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1f, -4.0f, -1f, 2, 8, 2),
				PartPose.offsetAndRotation(5f, 5, 3f, 0, 0, (float)Math.PI)
		);

		body.addOrReplaceChild(
				"gauge1",
				CubeListBuilder.create().texOffs(40, 13).addBox(-.5f, -1.5f, -.5f, 1, 3, 1),
				PartPose.offset(-3f, 5.5f, 5f)
		);
		body.addOrReplaceChild(
				"gauge2",
				CubeListBuilder.create().texOffs(40, 13).addBox(-.5f, -1.5f, -.5f, 1, 3, 1),
				PartPose.offset(1f, 5.5f, 5f)
		);
		body.addOrReplaceChild(
				"gauge3",
				CubeListBuilder.create().texOffs(44, 13).addBox(-2f, -.5f, -.5f, 3, 1, 1),
				PartPose.offset(-.5f, 3.5f, 5f)
		);
		body.addOrReplaceChild(
				"gauge4",
				CubeListBuilder.create().texOffs(44, 13).addBox(-2f, -.5f, -.5f, 3, 1, 1),
				PartPose.offset(-.5f, 7.5f, 5f)
		);

		body.addOrReplaceChild(
				"meterNeedle",
				CubeListBuilder.create().texOffs(52, 14).addBox(-.5f, -3.5f, -.5f, 1, 4, 1, new CubeDeformation(-.25f)),
				PartPose.offsetAndRotation(-1f, 7.625f, 5f, 0, 0, Mth.PI/4)
		);

		PartDefinition connector = part1.addOrReplaceChild("connector", CubeListBuilder.create(), PartPose.ZERO);
		connector.addOrReplaceChild(
				"part1",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3),
				PartPose.offset(-4.5f, 0f, 0f)
		);
		connector.addOrReplaceChild(
				"part2",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3, new CubeDeformation(-.375f)),
				PartPose.offset(-6f, 0f, 0)
		);
		connector.addOrReplaceChild(
				"part3",
				CubeListBuilder.create().texOffs(29, 9).addBox(-1f, -1.5f, -1.5f, 1, 3, 3),
				PartPose.offset(-6.25f, 0f, 0)
		);
		connector.addOrReplaceChild(
				"part4",
				CubeListBuilder.create().texOffs(12, 8).addBox(-.5f, -1f, -1f, 2, 2, 2),
				PartPose.offset(-7.5f, 0f, 0f)
		);
		connector.addOrReplaceChild(
				"part5",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3),
				PartPose.offsetAndRotation(4.5f, 0f, 0f, 0, Mth.PI, 0)
		);
		connector.addOrReplaceChild(
				"part6",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3, new CubeDeformation(-.375f)),
				PartPose.offsetAndRotation(6f, 0f, 0, 0, Mth.PI, 0)
		);
		connector.addOrReplaceChild(
				"part7",
				CubeListBuilder.create().texOffs(29, 9).addBox(-1f, -1.5f, -1.5f, 1, 3, 3),
				PartPose.offsetAndRotation(6.25f, 0f, 0, 0, Mth.PI, 0)
		);
		connector.addOrReplaceChild(
				"part8",
				CubeListBuilder.create().texOffs(12, 8).addBox(-.5f, -1f, -1f, 2, 2, 2),
				PartPose.offsetAndRotation(7.5f, 0f, 0f, 0, Mth.PI, 0)
		);

		for(int i = 0; i < 3; i++)
		{
			float pos = 3.125f-i*2.25f;
			PartDefinition tube = body.addOrReplaceChild(
					"tube"+i,
					CubeListBuilder.create().texOffs(56, 19).addBox(-1f, -1f, -1f, 2, 2, 2, new CubeDeformation(-.25f)),
					PartPose.offsetAndRotation(pos, 1, 5, -Mth.PI/4, 0, 0)
			);

			tube.addOrReplaceChild(
					"part1",
					CubeListBuilder.create().texOffs(52, 18).addBox(-.5f, -2f, -.5f, 1, 3, 1),
					PartPose.offset(0, -.5f, 0)
			);
			tube.addOrReplaceChild(
					"part2",
					CubeListBuilder.create().texOffs(56, 15).addBox(-1f, -2f, -1f, 2, 2, 2),
					PartPose.offset(0, -.75f, 0)
			);
			tube.addOrReplaceChild(
					"part3",
					CubeListBuilder.create().texOffs(56, 13).addBox(-.5f, -1f, -.5f, 1, 1, 1),
					PartPose.offset(0, -2.25f, 0)
			);
			tube.addOrReplaceChild(
					"part4",
					CubeListBuilder.create().texOffs(56, 13).addBox(-.5f, -1f, -.5f, 1, 1, 1, new CubeDeformation(-.25f)),
					PartPose.offset(0, -3f, 0)
			);
		}
		return LayerDefinition.create(data, 64, 32);
	}
}
