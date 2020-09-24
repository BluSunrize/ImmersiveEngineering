function initializeCoreMod() {
    return {
        'IE render hook after tile rendering': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.WorldRenderer',
                'methodName': 'func_228426_a_',
                'methodDesc': '(Lcom/mojang/blaze3d/matrix/MatrixStack;FJZLnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/util/math/vector/Matrix4f;)V'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var opcodes = Java.type('org.objectweb.asm.Opcodes');
                var callback = ASMAPI.listOf(
                    ASMAPI.buildMethodCall(
                        "blusunrize/immersiveengineering/client/utils/VertexBufferHolder",
                        "afterTERRendering",
                        "()V",
                        ASMAPI.MethodType.STATIC
                    ));
                var targetString = "destroyProgress";
                var inserted = false;
                for (var i = 0; i < method.instructions.size(); ++i) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() === opcodes.LDC && node.cst.equals(targetString)) {
                        var targetIndex = i - 2;
                        method.instructions.insert(method.instructions.get(targetIndex), callback);
                        inserted = true;
                        break;
                    }
                }
                if (inserted) {
                    ASMAPI.log("INFO", "Inserted after-TER callback", {});
                } else {
                    ASMAPI.log("WARN", "Failed to insert after-TER callback", {});
                }
                return method;
            }
        }
    }
}