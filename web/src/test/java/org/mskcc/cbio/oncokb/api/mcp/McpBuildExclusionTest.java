package org.mskcc.cbio.oncokb.api.mcp;

import org.junit.Test;
import org.mskcc.cbio.oncokb.config.MvcConfigurationEnterprise;
import org.mskcc.cbio.oncokb.config.MvcConfigurationPublicAPI;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The no-token public build ({@code -P public-api}, i.e. public.api.oncokb.org) must never expose MCP,
 * and the enterprise build must only expose it when {@code mcp.enabled=true}.
 */
public class McpBuildExclusionTest {
    private static final String MCP_PACKAGE = McpController.class.getPackage().getName();

    @Test
    public void publicApiBuildDoesNotScanTheMcpPackage() {
        for (String base : MvcConfigurationPublicAPI.class.getAnnotation(ComponentScan.class).basePackages()) {
            assertFalse(base + " would component-scan " + MCP_PACKAGE,
                MCP_PACKAGE.equals(base) || MCP_PACKAGE.startsWith(base + "."));
        }
    }

    @Test
    public void enterpriseBuildScansMcpButOnlyBehindTheProperty() {
        assertTrue(Arrays.asList(MvcConfigurationEnterprise.class.getAnnotation(ComponentScan.class).basePackages()).contains(MCP_PACKAGE));
        for (Class<?> bean : new Class<?>[]{McpController.class, McpToolRegistry.class, McpResourceRegistry.class}) {
            Conditional conditional = bean.getAnnotation(Conditional.class);
            assertTrue(bean.getSimpleName() + " must be @Conditional", conditional != null);
            assertArrayEquals(new Class<?>[]{McpEnabledCondition.class}, conditional.value());
        }
    }

    @Test
    public void scanningTheMcpPackageRegistersNothingByDefault() {
        String previous = System.getProperty(McpSettings.ENABLED);
        System.clearProperty(McpSettings.ENABLED);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        try {
            context.scan(MCP_PACKAGE);
            context.refresh();
            assertEquals(0, context.getBeanNamesForType(McpController.class).length);
            assertEquals(0, context.getBeanNamesForType(McpToolRegistry.class).length);
            assertEquals(0, context.getBeanNamesForType(McpResourceRegistry.class).length);
        } finally {
            context.close();
            if (previous != null) {
                System.setProperty(McpSettings.ENABLED, previous);
            }
        }
    }
}
