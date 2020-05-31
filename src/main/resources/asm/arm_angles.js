function initializeCoreMod() {
    return {
        'IE arm angles': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.model.BipedModel',
                'methodName': 'func_225597_a_',
                'methodDesc': '(Lnet/minecraft/entity/LivingEntity;FFFFF)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode')
                var opcodes = Java.type('org.objectweb.asm.Opcodes');
                var callback = ASMAPI.listOf(
                    new VarInsnNode(opcodes.ALOAD, 0),
                    new VarInsnNode(opcodes.ALOAD, 1),
                    ASMAPI.buildMethodCall(
                        "blusunrize/immersiveengineering/client/ClientUtils",
                        "handleBipedRotations",
                        "(Lnet/minecraft/client/renderer/entity/model/BipedModel;Lnet/minecraft/entity/Entity;)V",
                        ASMAPI.MethodType.STATIC
                    ));
                var ret = ASMAPI.findFirstInstruction(method, opcodes.RETURN);
                if (ret===null) {
                    throw "Failed to find return instruction";
                }
                method.instructions.insertBefore(ret, callback);

                ASMAPI.log("INFO", "Inserted arm angle callback", {});
                return method;
            }
        }
    }
}