package dev.mrblackreal.client.util;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;

public class RenderUtil {

	private static final Minecraft mc = Minecraft.getMinecraft();

	private static final ResourceLocation shader = new ResourceLocation("shaders/post/blur.json");

	private static int lastScaleFactor;
	private static int lastScaleWidth, lastScaleHeight;

	private static ShaderGroup blurShader;
	private static Framebuffer buffer;

	private static void setShaderConfig(float intensity, float blurWidth, float blurHeight) {
		blurShader.getShaders().get(0).getShaderManager().getShaderUniform("Radius").set(intensity);
		blurShader.getShaders().get(1).getShaderManager().getShaderUniform("Radius").set(intensity);

		blurShader.getShaders().get(0).getShaderManager().getShaderUniform("BlurDir").set(blurWidth, blurHeight);
		blurShader.getShaders().get(1).getShaderManager().getShaderUniform("BlurDir").set(blurHeight, blurWidth);
	}

	public static void initFboAndShader(double d, int categoryY, int i, int j, float f) {
		try {
			blurShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
			blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
			buffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
			buffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void drawBluredRect(float x, float y, float width, float height, float intensity) {
		final ScaledResolution sr = new ScaledResolution(mc);

		intensity = Math.max(intensity, 1);

		int scaleFactor = sr.getScaleFactor();
		int scaledWidth = sr.getScaledWidth();
		int scaledHeight = sr.getScaledHeight();

		if (lastScaleFactor != scaleFactor || lastScaleWidth != scaledWidth || lastScaleHeight != scaledHeight || buffer == null || blurShader == null)
			initFboAndShader(intensity, scaledHeight, scaledHeight, scaledHeight, intensity);

		lastScaleFactor = scaleFactor;
		lastScaleWidth = scaledWidth;
		lastScaleHeight = scaledHeight;

		if (OpenGlHelper.isFramebufferEnabled()) {
			buffer.framebufferClear();

			GL11.glScissor((int) (x * scaleFactor), (int) (mc.displayHeight - (y * scaleFactor) - height * scaleFactor), (int) (width * scaleFactor), (int) (height) * scaleFactor);
			GL11.glEnable(GL11.GL_SCISSOR_TEST);

			setShaderConfig(intensity, 1, 0);
			buffer.bindFramebuffer(true);
			blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

			mc.getFramebuffer().bindFramebuffer(true);

			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
			GlStateManager.disableBlend();

			GL11.glScalef(scaleFactor * .5f, scaleFactor * .5f, 0);
		}
	}
}
