/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import org.joml.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.minecraft.core.Direction.*;

public class ModelConfigurableSides extends BakedIEModel
{
	private static final HashMap<String, ITextureNamer> TYPES = new HashMap<>();

	public enum Type
	{
		SIDE_TOP_BOTTOM(new ITextureNamer()
		{//horizontal, up, down
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?side.getSerializedName(): "side";
			}
		}),
		SIDE_VERTICAL(new ITextureNamer()
		{//horizontal, vertical
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?"up": "side";
			}
		}),
		VERTICAL(new ITextureNamer()
		{//vertical, sides not configureable
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?"up": "side";
			}

			@Override
			public String nameFromCfg(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?cfg.getTextureName(): null;
			}
		}),
		ALL_SAME_TEXTURE(new ITextureNamer()
		{//all sides, same texture
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return "side";
			}
		});
		private final ITextureNamer nameMapper;

		Type(ITextureNamer nameMapper)
		{
			this.nameMapper = nameMapper;
		}

		public String getName()
		{
			return name().toLowerCase(Locale.US);
		}
	}

	static
	{
		for(Type type : Type.values())
			TYPES.put(type.getName(), type.nameMapper);
	}

	private final LoadingCache<Map<Direction, IOSideConfig>, Map<Direction, BakedQuad>> modelCache = CacheBuilder.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build(CacheLoader.from(key -> {
				Map<Direction, TextureAtlasSprite> tex = new EnumMap<>(Direction.class);
				for(Direction d : DirectionUtils.VALUES)
					tex.put(d, this.textures.get(d).get(key.get(d)));
				return bakeQuads(tex);
			}));

	final String name;
	public Map<Direction, Map<IOSideConfig, TextureAtlasSprite>> textures;
	private final RenderTypeGroup renderTypes;

	public ModelConfigurableSides(
			String name, Map<Direction, Map<IOSideConfig, TextureAtlasSprite>> textures, RenderTypeGroup renderTypes
	)
	{
		this.name = name;
		this.textures = textures;
		this.renderTypes = renderTypes;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		if(side==null)
			return ImmutableList.of();
		Map<Direction, IOSideConfig> config;
		if(extraData.has(Model.SIDECONFIG))
			config = extraData.get(Model.SIDECONFIG);
		else
		{
			config = new EnumMap<>(Direction.class);
			for(Direction d : DirectionUtils.VALUES)
				config.put(d, IOSideConfig.NONE);
		}
		assert (config!=null);
		return ImmutableList.of(modelCache.getUnchecked(config).get(side));
	}

	@Nonnull
	@Override
	public ModelData getModelData(@Nonnull BlockAndTintGetter world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData)
	{
		ModelData.Builder data = super.getModelData(world, pos, state, tileData).derive();
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof IConfigurableSides confTE)
		{
			Map<Direction, IOSideConfig> conf = new EnumMap<>(Direction.class);
			for(Direction d : DirectionUtils.VALUES)
				conf.put(d, confTE.getSideConfig(d));
			data.with(Model.SIDECONFIG, conf);
		}
		return data.build();
	}

	private static Map<Direction, BakedQuad> bakeQuads(Map<Direction, TextureAtlasSprite> sprites)
	{
		Map<Direction, BakedQuad> quads = new EnumMap<>(Direction.class);
		Vec3[] vertices = {new Vec3(0, 0, 0), new Vec3(0, 0, 1), new Vec3(1, 0, 1), new Vec3(1, 0, 0)};
		addQuad(quads, sprites, DOWN, vertices, new double[]{0, 16, 16, 0});
		vertices = new Vec3[]{new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, 0)};
		addQuad(quads, sprites, UP, vertices, new double[]{0, 0, 16, 16});

		vertices = new Vec3[]{new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 0)};
		addQuad(quads, sprites, NORTH, vertices, new double[]{0, 16, 16, 0});
		vertices = new Vec3[]{new Vec3(1, 0, 1), new Vec3(1, 1, 1), new Vec3(0, 1, 1), new Vec3(0, 0, 1)};
		addQuad(quads, sprites, SOUTH, vertices, new double[]{16, 16, 0, 0});

		vertices = new Vec3[]{new Vec3(0, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(0, 0, 1)};
		addQuad(quads, sprites, WEST, vertices, new double[]{0, 16, 16, 0});
		vertices = new Vec3[]{new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(1, 1, 1), new Vec3(1, 0, 1)};
		addQuad(quads, sprites, EAST, vertices, new double[]{16, 16, 0, 0});
		return quads;
	}

	private static void addQuad(
			Map<Direction, BakedQuad> out, Map<Direction, TextureAtlasSprite> sprites,
			Direction side, Vec3[] vertices, double[] uv
	)
	{
		out.put(side, ModelUtils.createBakedQuad(
				vertices, side, sprites.get(side), uv, new float[]{1, 1, 1, 1}, side.getAxisDirection()==AxisDirection.NEGATIVE
		));
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return this.textures.get(DOWN).get(IOSideConfig.NONE);
	}

	static final ItemTransforms defaultTransforms = new ItemTransforms(
			new ItemTransform(new Vector3f(75, 45, 0), new Vector3f(0, .25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)), //thirdperson left
			new ItemTransform(new Vector3f(75, 45, 0), new Vector3f(0, .15625f, 0), new Vector3f(0.375f, 0.375f, 0.375f)), //thirdperson left

			new ItemTransform(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(.4f, .4f, .4f)), //firstperson left
			new ItemTransform(new Vector3f(0, 225, 0), new Vector3f(0, 0, 0), new Vector3f(.4f, .4f, .4f)), //firstperson right

			new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), //head
			new ItemTransform(new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(.625f, .625f, .625f)), //gui
			new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, .1875f, 0), new Vector3f(.25f, .25f, .25f)), //ground
			new ItemTransform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(.5f, .5f, .5f))); //fixed

	@Nonnull
	@Override
	public ItemTransforms getTransforms()
	{
		return defaultTransforms;
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

	@Override
	public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data)
	{
		return ChunkRenderTypeSet.of(renderTypes.block());
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous)
	{
		return List.of(fabulous?renderTypes.entityFabulous(): renderTypes.entity());
	}

	public static class Loader implements IGeometryLoader<ConfigSidesModelBase>
	{
		public static ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "conf_sides");

		@Nonnull
		@Override
		public ConfigSidesModelBase read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext)
		{
			final String name = modelContents.get("base_name").getAsString();
			final String type = modelContents.get("type").getAsString();
			ImmutableMap.Builder<String, Material> builder = ImmutableMap.builder();
			ITextureNamer namer = TYPES.get(type);
			for(Direction f : DirectionUtils.VALUES)
				for(IOSideConfig cfg : IOSideConfig.values())
				{
					String key = f.getSerializedName()+"_"+cfg.getTextureName();
					String tex = name+"_"+namer.getTextureName(f, cfg);
					builder.put(key, new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(tex)));
				}
			return new ConfigSidesModelBase(name, type, builder.build());
		}
	}

	private record ConfigSidesModelBase(
			String name, String type, Map<String, Material> textures
	) implements IUnbakedGeometry<ConfigSidesModelBase>
	{

		@Override
		public BakedModel bake(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
		{
			Map<Direction, Map<IOSideConfig, TextureAtlasSprite>> tex = new EnumMap<>(Direction.class);
			for(Direction f : DirectionUtils.VALUES)
			{
				Map<IOSideConfig, TextureAtlasSprite> forSide = new EnumMap<>(IOSideConfig.class);
				for(IOSideConfig cfg : IOSideConfig.values())
				{
					Material rl = textures.get(f.getSerializedName()+"_"+cfg.getTextureName());
					if(rl!=null)
						forSide.put(cfg, spriteGetter.apply(rl));
				}
				tex.put(f, forSide);
			}
			final ResourceLocation renderTypeName = Objects.requireNonNullElseGet(
					owner.getRenderTypeHint(), () -> new ResourceLocation("solid")
			);
			return new ModelConfigurableSides(name, tex, owner.getRenderType(renderTypeName));
		}
	}

	interface ITextureNamer
	{
		default String getTextureName(Direction side, IOSideConfig cfg)
		{
			String s = nameFromSide(side, cfg);
			String c = nameFromCfg(side, cfg);
			if(s!=null&&c!=null)
				return s+"_"+c;
			else if(s!=null)
				return s;
			else if(c!=null)
				return c;
			return "";
		}

		default String nameFromSide(Direction side, IOSideConfig cfg)
		{
			return side.getSerializedName();
		}

		default String nameFromCfg(Direction side, IOSideConfig cfg)
		{
			return cfg.getTextureName();
		}
	}
}
