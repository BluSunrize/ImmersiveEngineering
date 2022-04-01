/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.mixin;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.code.Injector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.AnnotationType;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo.HandlerPrefix;
import org.spongepowered.asm.mixin.transformer.MixinTargetContext;

@AnnotationType(CaptureOwner.class)
@HandlerPrefix("captureOwner")
public class CaptureOwnerInjectionInfo extends InjectionInfo
{
	public CaptureOwnerInjectionInfo(MixinTargetContext mixin, MethodNode method, AnnotationNode annotation)
	{
		super(mixin, method, annotation);
	}

	@Override
	protected Injector parseInjector(AnnotationNode injectAnnotation)
	{
		return new CaptureOwnerInjector(this);
	}
}
