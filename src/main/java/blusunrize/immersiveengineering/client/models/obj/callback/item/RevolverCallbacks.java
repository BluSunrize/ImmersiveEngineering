/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.RevolverContainer;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.RevolverItem.SpecialRevolver;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;

import javax.annotation.Nonnull;
import java.util.*;

import static blusunrize.immersiveengineering.common.items.RevolverItem.specialRevolversByTag;

public class RevolverCallbacks implements ItemCallback<RevolverCallbacks.Key>
{
	public static final RevolverCallbacks INSTANCE = new RevolverCallbacks();

	public static HashMap<String, TextureAtlasSprite> revolverIcons = new HashMap<>();
	public static TextureAtlasSprite revolverDefaultTexture;

	/*TODO move to JSON
	public static void addRevolverTextures(TextureStitchEvent.Pre evt)
	{
		evt.addSprite(new ResourceLocation(ImmersiveEngineering.MODID, "revolvers/revolver"));
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty()&&!specialRevolversByTag.get(key).tag.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split < 0)
					split = key.length();
				evt.addSprite(new ResourceLocation(ImmersiveEngineering.MODID, "revolvers/revolver_"+key.substring(0, split).toLowerCase(Locale.US)));
			}
	}*/

	public static void retrieveRevolverTextures(TextureAtlas map)
	{
		revolverDefaultTexture = map.getSprite(new ResourceLocation("immersiveengineering:revolvers/revolver"));
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty()&&!specialRevolversByTag.get(key).tag.isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split < 0)
					split = key.length();
				revolverIcons.put(key, map.getSprite(new ResourceLocation("immersiveengineering:revolvers/revolver_"+key.substring(0, split).toLowerCase(Locale.US))));
			}
	}

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = RevolverItem.getUpgradesStatic(stack);
		return new Key(
				ItemNBTHelper.getString(stack, "elite"),
				ItemNBTHelper.getString(stack, "flavour"),
				upgrades.getInt("bullets") > 0,
				upgrades.getBoolean("fancyAnimation"),
				upgrades.getDouble("melee") > 0,
				upgrades.getBoolean("electro"),
				upgrades.getBoolean("scope"),
				ItemNBTHelper.getInt(stack, "reload")
		);
	}

	@Override
	public TextureAtlasSprite getTextureReplacement(Key stack, String group, String material)
	{
		if(!stack.elite().isEmpty())
			return revolverIcons.get(stack.elite());
		else
			return revolverDefaultTexture;
	}

	@Override
	public boolean shouldRenderGroup(Key stack, String group, RenderType layer)
	{
		if(group.equals("frame")||group.equals("cylinder")||group.equals("barrel")||group.equals("cosmetic_compensator"))
			return true;

		Set<String> render = new HashSet<>();
		String tag = stack.elite();
		String flavour = stack.flavor();
		if(!tag.isEmpty()&&specialRevolversByTag.containsKey(tag))
		{
			SpecialRevolver r = specialRevolversByTag.get(tag);
			if(r!=null&&r.renderAdditions!=null)
				Collections.addAll(render, r.renderAdditions);
		}
		else if(!flavour.isEmpty()&&specialRevolversByTag.containsKey(flavour))
		{
			SpecialRevolver r = specialRevolversByTag.get(flavour);
			if(r!=null&&r.renderAdditions!=null)
				Collections.addAll(render, r.renderAdditions);
		}
		if(stack.extraBullets()&&!render.contains("dev_mag"))
			render.add("player_mag");
		if(stack.extraMelee()&&!render.contains("dev_bayonet"))
		{
			render.add("bayonet_attachment");
			render.add("player_bayonet");
		}
		if(stack.electro())
		{
			render.add("player_electro_0");
			render.add("player_electro_1");
		}
		if(stack.scope())
			render.add("dev_scope");
		return render.contains(group);
	}

	@Override
	public void handlePerspective(Key key, LivingEntity holder, TransformType cameraTransformType, PoseStack mat)
	{
		if(holder instanceof Player player&&(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND||cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND||cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)==(holder.getMainArm()==HumanoidArm.RIGHT);
			boolean left = cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND;
			if(key.fancyAnimation()&&main)
			{
				float f = player.getAttackStrengthScale(ClientUtils.mc().getFrameTime());
				if(f < 1)
				{
					float angle = f*-6.28318f;
					if(left)
						angle *= -1;
					mat.translate(0, 1.5-f, 0);
					mat.mulPose(new Quaternionf().rotateXYZ(0, 0, angle));
				}
			}

			//Re-grab stack because the other one doesn't do reloads properly
			if(key.reload() > 0)
			{
				float f = 3-key.reload/20f; //Reload time in seconds, for coordinating with audio
				if(f > .35&&f < 1.95)
					if(f < .5)
					{
						mat.translate((.35-f)*2, 0, 0);
						mat.mulPose(new Quaternionf().rotateAxis(2.64F*(f-.35F), new Vector3f(0, 0, left?-1: 1)));
					}
					else if(f < .6)
					{
						mat.translate((f-.5)*6, (.5-f)*1, 0);
						mat.mulPose(new Quaternionf().rotateAxis(.87266F, new Vector3f(0, 0, left?-1: 1)));
					}
					else if(f < 1.7)
					{
						mat.translate(0, -.6, 0);
						mat.mulPose(new Quaternionf().rotateAxis(.87266F, new Vector3f(0, 0, left?-1: 1)));
					}
					else if(f < 1.8)
					{
						mat.translate((1.8-f)*6, (f-1.8)*1, 0);
						mat.mulPose(new Quaternionf().rotateAxis(.87266F, new Vector3f(0, 0, left?-1: 1)));
					}
					else
					{
						mat.translate((f-1.95f)*2, 0, 0);
						mat.mulPose(new Quaternionf().rotateAxis(2.64F*(1.95F-f), new Vector3f(0, 0, left?-1: 1)));
					}
			}
			else if(player.containerMenu instanceof RevolverContainer)
			{
				mat.translate(left?.4: -.4, .4, 0);
				mat.mulPose(new Quaternionf().rotateAxis(.87266F, 0, 0, left?-1: 1));
			}
		}
	}

	private static final List<List<String>> groups = List.of(List.of("frame"), List.of("cylinder"));

	@Override
	public List<List<String>> getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return groups;
	}

	private static Transformation matOpen;
	private static Transformation matClose;
	private static Transformation matCylinder;

	@Nonnull
	@Override
	public Transformation getTransformForGroups(ItemStack stack, List<String> groups, TransformType transform, LivingEntity entity,
												float partialTicks)
	{
		if(matOpen==null)
			matOpen = new Transformation(new Vector3f(-.625F, .25F, 0), new Quaternionf().rotateXYZ(0, 0, -.87266f), null, null);
		if(matClose==null)
			matClose = new Transformation(new Vector3f(-.625F, .25F, 0), null, null, null);
		if(matCylinder==null)
			matCylinder = new Transformation(new Vector3f(0, .6875F, 0), null, null, null);
		if(entity instanceof Player&&(transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.FIRST_PERSON_LEFT_HAND||transform==TransformType.THIRD_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_RIGHT_HAND)==(entity.getMainArm()==HumanoidArm.RIGHT);
			boolean left = transform==TransformType.FIRST_PERSON_LEFT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND;
			//Re-grab stack because the other one doesn't do reloads properly
			stack = main?entity.getMainHandItem(): entity.getOffhandItem();
			if(ItemNBTHelper.hasKey(stack, "reload"))
			{
				float f = 3-ItemNBTHelper.getInt(stack, "reload")/20f; //Reload time in seconds, for coordinating with audio
				if("frame".equals(groups.get(0)))
				{
					if(f < .35||f > 1.95)
						return matClose;
					else if(f < .5)
						return new Transformation(
								new Vector3f(-.625f, .25f, 0),
								new Quaternionf().rotateXYZ(0, 0, -2.64F*(f-.35F)),
								null, null);
					else if(f < 1.8)
						return matOpen;
					else
						return new Transformation(
								new Vector3f(-.625f, .25f, 0),
								new Quaternionf().rotateXYZ(0, 0, -2.64f*(1.95f-f)),
								null, null);
				}
				else if(f > 2.5&&f < 2.9)
				{
					float angle = (left?-1: 1)*-15.70795f*(f-2.5f);
					return new Transformation(
							new Vector3f(0, .6875f, 0),
							new Quaternionf().rotateXYZ(angle, 0, 0),
							null, null);
				}
			}
			else if("frame".equals(groups.get(0))&&((Player)entity).containerMenu instanceof RevolverContainer)
				return matOpen;
		}
		return "frame".equals(groups.get(0))?matClose: matCylinder;
	}

	public record Key(
			String elite, String flavor, boolean extraBullets, boolean fancyAnimation, boolean extraMelee,
			boolean electro, boolean scope, int reload
	)
	{
	}
}
