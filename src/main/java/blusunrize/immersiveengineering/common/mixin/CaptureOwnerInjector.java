/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.mixin;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.invoke.InvokeInjector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.struct.Target.Extension;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;

import java.util.List;

public class CaptureOwnerInjector extends InvokeInjector
{
	public CaptureOwnerInjector(InjectionInfo info)
	{
		super(info, "@CaptureOwner");
	}

	@Override
	protected void sanityCheck(Target target, List<InjectionPoint> injectionPoints)
	{
		super.sanityCheck(target, injectionPoints);
		if(this.returnType!=Type.VOID_TYPE)
		{
			throw new InvalidInjectionException(
					this.info, "Target for CaptureOwner must return void, not "+this.returnType
			);
		}
		if(this.methodArgs.length!=1)
		{
			throw new InvalidInjectionException(
					this.info, "Target for CaptureOwner must take exactly one argument, not "+this.methodArgs.length
			);
		}
		// Do not check parameter type, this will be some superclass of the classes we get at the injection points and I
		// cba to figure out how to check that from here. Also, this is not API, it's internal use only, so you better
		// know what you're doing!
	}

	@Override
	protected void injectAtInvoke(Target target, InjectionNode node)
	{
		MethodInsnNode invokedNode = (MethodInsnNode)node.getCurrentTarget();
		Type[] realArgs = Type.getArgumentTypes(invokedNode.desc);
		// Top part of the stack before the method call
		Type[] argsWithThis = new Type[realArgs.length+1];
		argsWithThis[0] = Type.getType('L'+invokedNode.owner+';');
		System.arraycopy(realArgs, 0, argsWithThis, 1, realArgs.length);

		InsnList injected = new InsnList();
		Extension extraLocals = target.extendLocals();
		// Move method args&owner from stack into locals
		int[] storedArgs = storeArgs(target, argsWithThis, injected, 0);
		// Load owner and call handler
		invokeHandlerWithArgs(argsWithThis, injected, storedArgs, 0, 1);
		// Restore stack state
		pushArgs(argsWithThis, injected, storedArgs, 0, storedArgs.length);
		extraLocals.add(storedArgs[storedArgs.length-1]-target.getMaxLocals());
		target.insns.insertBefore(invokedNode, injected);
		target.extendStack().set(2-(extraLocals.get()-1)).apply();
		extraLocals.apply();
	}
}
