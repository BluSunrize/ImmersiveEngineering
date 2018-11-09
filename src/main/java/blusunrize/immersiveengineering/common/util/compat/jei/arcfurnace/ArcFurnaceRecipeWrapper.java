/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei.arcfurnace;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.jei.MultiblockRecipeWrapper;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;

public class ArcFurnaceRecipeWrapper extends MultiblockRecipeWrapper
{
	public String specialRecipeType;
	int time;
	int energy;

	public ArcFurnaceRecipeWrapper(ArcFurnaceRecipe recipe)
	{
		super(recipe);
		String name = this.getClass().getName();
		int idx = name.indexOf("ArcFurnaceRecipeWrapper");
		if(idx >= 0&&idx+"ArcFurnaceRecipeWrapper".length() < name.length())
			specialRecipeType = name.substring(idx+"ArcFurnaceRecipeWrapper".length());
		time = recipe.getTotalProcessTime();
		energy = recipe.getTotalProcessEnergy()/time;
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY)
	{
		String s = energy+" IF/t";
		minecraft.fontRenderer.drawString(s, 54, 38, 0x777777);
		s = time+" ticks";
		minecraft.fontRenderer.drawString(s, 54, 48, 0x777777);
	}

	public static ArcFurnaceRecipeWrapper getWrapper(ArcFurnaceRecipe recipe)
	{
		if(recipe.specialRecipeType==null)
			return new ArcFurnaceRecipeWrapper(recipe);
		else
		{
			try
			{
				return getWrapperClass(recipe.specialRecipeType).getConstructor(ArcFurnaceRecipe.class).newInstance(recipe);
			} catch(Exception e)
			{
				IELogger.error("Adding an ArcFurnaceRecipe: "+recipe.specialRecipeType+", threw an error! Report this!");
				e.printStackTrace();
			}
		}
		return new ArcFurnaceRecipeWrapper(recipe);
	}

	private static HashMap<String, Class<? extends ArcFurnaceRecipeWrapper>> dynamicSubclasses = new HashMap();

	public static Class<? extends ArcFurnaceRecipeWrapper> getWrapperClass(String subtype)
	{
		if(subtype==null)
			return ArcFurnaceRecipeWrapper.class;
		if(dynamicSubclasses.containsKey(subtype))
			return dynamicSubclasses.get(subtype);
		try
		{
			Class<? extends ArcFurnaceRecipeWrapper> subWrapper = createSubWrapper(subtype);
			if(subWrapper!=null)
			{
				dynamicSubclasses.put(subtype, subWrapper);
				//Can't add Categories dynamically anymore =c
				//ArcFurnaceRecipeCategory cat = new ArcFurnaceRecipeCategory(JEIHelper.jeiHelpers.getGuiHelper(), subtype, subWrapper);
				//JEIHelper.modRegistry.addRecipeCategories(cat);
				//JEIHelper.modRegistry.addRecipeHandlers(cat);
				return subWrapper;
			}
		} catch(Exception e)
		{
			IELogger.error("The dynamic JEI recipe handler for the ArcFurnace - "+subtype+", threw an error! Report this!");
			e.printStackTrace();
		}
		return ArcFurnaceRecipeWrapper.class;
	}

	private static Class<? extends ArcFurnaceRecipeWrapper> createSubWrapper(String subtype) throws Exception
	{
		String entitySuperClassName = Type.getInternalName(ArcFurnaceRecipeWrapper.class);
		String entityProxySubClassName = ArcFurnaceRecipeWrapper.class.getSimpleName().concat(subtype);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC+Opcodes.ACC_SUPER, entityProxySubClassName, null, entitySuperClassName, null);
		cw.visitSource(entityProxySubClassName.concat(".java"), null);
		//create constructor
		String methodDescriptor = "(L"+Type.getInternalName(ArcFurnaceRecipe.class)+";)V";
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", methodDescriptor, null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, entitySuperClassName, "<init>", methodDescriptor, false);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		cw.visitEnd();
		return (Class<? extends ArcFurnaceRecipeWrapper>)new ProxyClassLoader(Thread.currentThread().getContextClassLoader(), cw.toByteArray()).loadClass(entityProxySubClassName.replaceAll("/", "."));
	}

	public static class ProxyClassLoader extends ClassLoader
	{
		private byte[] rawClassBytes;

		public ProxyClassLoader(ClassLoader parentClassLoader, byte[] classBytes)
		{
			super(parentClassLoader);
			this.rawClassBytes = classBytes;
		}

		@Override
		public Class findClass(String name)
		{
			return defineClass(name, this.rawClassBytes, 0, this.rawClassBytes.length);
		}
	}
}