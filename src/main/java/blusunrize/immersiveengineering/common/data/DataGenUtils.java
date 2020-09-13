/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.data;

import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataGenUtils
{
	private static final Pattern MTLLIB = Pattern.compile("^mtllib\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern USEMTL = Pattern.compile("^usemtl\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern NEWMTL = Pattern.compile("^newmtl\\s+(.*)$", Pattern.MULTILINE);
	private static final Pattern MAP_KD = Pattern.compile("^map_Kd\\s+(.*)$", Pattern.MULTILINE);

	public static String getTextureFromObj(ResourceLocation obj, ExistingFileHelper helper)
	{
		try
		{
			String prefix = "models";
			if (obj.getPath().startsWith("models/"))
				prefix = "";
			IResource objResource = helper.getResource(obj, ResourcePackType.CLIENT_RESOURCES, "", prefix);
			InputStream objStream = objResource.getInputStream();
			String fullObj = IOUtils.toString(objStream, StandardCharsets.US_ASCII);
			String libLoc = findFirstOccurrenceGroup(MTLLIB, fullObj);
			String libName = findFirstOccurrenceGroup(USEMTL, fullObj);
			ResourceLocation libRL = relative(obj, libLoc);
			return getMTLTexture(libRL, libName, helper);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static String getMTLTexture(ResourceLocation mtl, String materialName, ExistingFileHelper helper)
	{
		try
		{
			IResource mtlResource = helper.getResource(mtl, ResourcePackType.CLIENT_RESOURCES, "", "models");
			String fullMtl = IOUtils.toString(mtlResource.getInputStream(), StandardCharsets.US_ASCII);
			Matcher materialMatcher = NEWMTL.matcher(fullMtl);
			while(materialMatcher.find()&&!materialMatcher.group(1).equals(materialName)) ;
			int materialStart = materialMatcher.start();
			int materialEnd;
			if(materialMatcher.find())
				materialEnd = materialMatcher.start();
			else
				materialEnd = fullMtl.length();
			String material = fullMtl.substring(materialStart, materialEnd);
			return findFirstOccurrenceGroup(MAP_KD, material);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static String findFirstOccurrenceGroup(Pattern pattern, String input)
	{
		Matcher matcher = pattern.matcher(input);
		Preconditions.checkArgument(matcher.find());
		return matcher.group(1);
	}

	private static ResourceLocation relative(ResourceLocation base, String relativePath)
	{
		String basePath = base.getPath();
		String lastDir = basePath.substring(0, basePath.lastIndexOf('/')+1);
		return new ResourceLocation(base.getNamespace(), lastDir+relativePath);
	}

	public static INamedTag<Item> createItemWrapper(ResourceLocation name)
	{
		Optional<? extends INamedTag<Item>> existing = ItemTags.func_242177_b()
				.stream()
				.filter(tag -> tag.getName().equals(name))
				.findAny();
		if(existing.isPresent())
			return existing.get();
		else
			return ItemTags.makeWrapperTag(name.toString());
	}

	public static INamedTag<Block> createBlockWrapper(ResourceLocation name) {
		//TODO deduplicate, maybe speed up?
		Optional<? extends INamedTag<Block>> existing = BlockTags.func_242174_b()
				.stream()
				.filter(tag -> tag.getName().equals(name))
				.findAny();
		if(existing.isPresent())
			return existing.get();
		else
			return BlockTags.makeWrapperTag(name.toString());
	}

	public static INamedTag<Fluid> createFluidWrapper(ResourceLocation name)
	{
		return FluidTags.makeWrapperTag(name.toString());
	}
}
