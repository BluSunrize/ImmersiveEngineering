/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.tool.ZoomHandler;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.item.RevolverCallbacks.Key;
import blusunrize.immersiveengineering.common.gui.RevolverContainer;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.RevolverItem.RevolverCooldowns;
import blusunrize.immersiveengineering.common.items.RevolverItem.SpecialRevolver;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.common.items.RevolverItem.specialRevolversByTag;

public class RevolverCallbacks implements ItemCallback<Key>
{
	public static final RevolverCallbacks INSTANCE = new RevolverCallbacks();

	public static HashMap<String, TextureAtlasSprite> revolverIcons = new HashMap<>();
	public static TextureAtlasSprite revolverDefaultTexture;

	public static void retrieveRevolverTextures(TextureAtlas map)
	{
		revolverDefaultTexture = map.getSprite(revolverRL("revolver"));
		for(String key : specialRevolversByTag.keySet())
			if(!key.isEmpty()&&!specialRevolversByTag.get(key).tag().isEmpty())
			{
				int split = key.lastIndexOf("_");
				if(split < 0)
					split = key.length();
				revolverIcons.put(key, map.getSprite(revolverRL("revolver_"+key.substring(0, split).toLowerCase(Locale.US))));
			}
	}

	private static ResourceLocation revolverRL(String revolverName)
	{
		return rl("item/revolvers/"+revolverName);
	}

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		var upgrades = RevolverItem.getUpgradesStatic(stack);
		return new Key(
				stack.getOrDefault(IEDataComponents.REVOLVER_ELITE, ""),
				stack.getOrDefault(IEDataComponents.REVOLVER_FLAVOUR, ""),
				upgrades.get(UpgradeEffect.BULLETS) > 0,
				upgrades.has(UpgradeEffect.FANCY_ANIMATION),
				upgrades.get(UpgradeEffect.MELEE) > 0,
				upgrades.has(UpgradeEffect.ELECTRO),
				upgrades.has(UpgradeEffect.SCOPE),
				stack.getOrDefault(IEDataComponents.REVOLVER_COOLDOWN, RevolverCooldowns.DEFAULT).reloadTimer(),
				upgrades.has(UpgradeEffect.BARREL_SNUB)
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
		if(group.equals("frame")||group.equals("cylinder"))
			return true;

		Set<String> render = new HashSet<>();
		String tag = stack.elite();
		String flavour = stack.flavor();
		if(!tag.isEmpty()&&specialRevolversByTag.containsKey(tag))
		{
			SpecialRevolver r = specialRevolversByTag.get(tag);
			if(r!=null&&r.renderAdditions()!=null)
				render.addAll(r.renderAdditions());
		}
		else if(!flavour.isEmpty()&&specialRevolversByTag.containsKey(flavour))
		{
			SpecialRevolver r = specialRevolversByTag.get(flavour);
			if(r!=null&&r.renderAdditions()!=null)
				render.addAll(r.renderAdditions());
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
		if(stack.snub())
			render.add("barrel_snub");
		else
		{
			render.add("barrel");
			render.add("cosmetic_compensator");
		}
		return render.contains(group);
	}

	@Override
	public void handlePerspective(Key key, LivingEntity holder, ItemDisplayContext cameraItemDisplayContext, PoseStack mat)
	{
		if(ZoomHandler.isZooming && (cameraItemDisplayContext==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||cameraItemDisplayContext==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND))
		{
			// hide the item in first person when zooming
			mat.scale(0f,0f,0f);
			return;
		}

		if(holder instanceof Player player&&(cameraItemDisplayContext==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||cameraItemDisplayContext==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||cameraItemDisplayContext==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND||cameraItemDisplayContext==ItemDisplayContext.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (cameraItemDisplayContext==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||cameraItemDisplayContext==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)==(holder.getMainArm()==HumanoidArm.RIGHT);
			boolean left = cameraItemDisplayContext==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||cameraItemDisplayContext==ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
			if(key.fancyAnimation()&&main)
			{
				float f = player.getAttackStrengthScale(ClientUtils.mc().getTimer().getGameTimeDeltaTicks());
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
						mat.mulPose(new Quaternionf().rotateAxis(2.64F*(f-.35F), new Vector3f(0, 0, 1)));
					}
					else if(f < .6)
					{
						mat.translate((f-.5)*6, (.5-f)*1, 0);
						mat.mulPose(new Quaternionf().rotateAxis(.87266F, new Vector3f(0, 0, 1)));
					}
					else if(f < 1.7)
					{
						mat.translate(0, -.6, 0);
						mat.mulPose(new Quaternionf().rotateAxis(.87266F, new Vector3f(0, 0, 1)));
					}
					else if(f < 1.8)
					{
						mat.translate((1.8-f)*6, (f-1.8)*1, 0);
						mat.mulPose(new Quaternionf().rotateAxis(.87266F, new Vector3f(0, 0, 1)));
					}
					else
					{
						mat.translate((f-1.95f)*2, 0, 0);
						mat.mulPose(new Quaternionf().rotateAxis(2.64F*(1.95F-f), new Vector3f(0, 0, 1)));
					}
			}
			else if(player.containerMenu instanceof RevolverContainer)
			{
				mat.translate(left?.4: -.4, .4, 0);
				mat.mulPose(new Quaternionf().rotateAxis(.87266F, 0, 0, 1));
			}
		}
	}

	private static final List<List<String>> SPECIAL_GROUPS = List.of(
			List.of("frame"),
			List.of("cylinder"),
			List.of("dev_scope"),
			List.of("player_electro_0", "player_electro_1")
	);

	@Override
	public List<List<String>> getSpecialGroups(ItemStack stack, ItemDisplayContext transform, LivingEntity entity)
	{
		return SPECIAL_GROUPS;
	}

	private static final Transformation MAT_OPEN = new Transformation(new Vector3f(-.625F, .25F, 0), new Quaternionf().rotateXYZ(0, 0, -.87266f), null, null);
	private static final Transformation MAT_CLOSE = new Transformation(new Vector3f(-.625F, .25F, 0), null, null, null);
	private static final Transformation MAT_CYLINDER = new Transformation(new Vector3f(0, .6875F, 0), null, null, null);
	private static final Transformation MAT_SNUB_SCOPE = new Transformation(new Vector3f(1.375f, 0, 0), null, null, null);
	private static final Transformation MAT_SNUB_ELECTRO = new Transformation(new Vector3f(1.4375f, -.1875f, 0.125f), null, null, null);

	@Nonnull
	@Override
	public Transformation getTransformForGroups(ItemStack stack, List<String> groups, ItemDisplayContext transform, LivingEntity entity,
												float partialTicks)
	{
		String firstGroup = groups.get(0);
		// special case for snubnose, move attachments back
		if(RevolverItem.getUpgradesStatic(stack).has(UpgradeEffect.BARREL_SNUB))
		{
			if("dev_scope".equals(firstGroup))
				return MAT_SNUB_SCOPE;
			else if("player_electro_0".equals(firstGroup))
				return MAT_SNUB_ELECTRO;
		}

		if(entity instanceof Player&&(transform==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||transform==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||transform==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND||transform==ItemDisplayContext.THIRD_PERSON_LEFT_HAND))
		{
			boolean main = (transform==ItemDisplayContext.FIRST_PERSON_RIGHT_HAND||transform==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)==(entity.getMainArm()==HumanoidArm.RIGHT);
			boolean left = transform==ItemDisplayContext.FIRST_PERSON_LEFT_HAND||transform==ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
			//Re-grab stack because the other one doesn't do reloads properly
			stack = main?entity.getMainHandItem(): entity.getOffhandItem();
			var cooldowns = RevolverItem.getCooldowns(stack);
			if(cooldowns.reloadTimer() > 0)
			{
				float f = 3-cooldowns.reloadTimer()/20f; //Reload time in seconds, for coordinating with audio
				if("frame".equals(firstGroup))
				{
					if(f < .35||f > 1.95)
						return MAT_CLOSE;
					else if(f < .5)
						return new Transformation(
								new Vector3f(-.625f, .25f, 0),
								new Quaternionf().rotateXYZ(0, 0, -2.64F*(f-.35F)),
								null, null);
					else if(f < 1.8)
						return MAT_OPEN;
					else
						return new Transformation(
								new Vector3f(-.625f, .25f, 0),
								new Quaternionf().rotateXYZ(0, 0, -2.64f*(1.95f-f)),
								null, null);
				}
				else if("cylinder".equals(firstGroup)&&f > 2.5&&f < 2.9)
				{
					float angle = (left?-1: 1)*-15.70795f*(f-2.5f);
					return new Transformation(
							new Vector3f(0, .6875f, 0),
							new Quaternionf().rotateXYZ(angle, 0, 0),
							null, null);
				}
			}
			else if("frame".equals(firstGroup)&&((Player)entity).containerMenu instanceof RevolverContainer)
				return MAT_OPEN;
		}
		return switch(firstGroup)
		{
			case "frame" -> MAT_CLOSE;
			case "cylinder" -> MAT_CYLINDER;
			default -> Transformation.identity();
		};
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key("", "", false, false, false, false, false, 0, false);
	}

	public record Key(
			String elite, String flavor, boolean extraBullets, boolean fancyAnimation, boolean extraMelee,
			boolean electro, boolean scope, int reload, boolean snub
	)
	{
	}
}
