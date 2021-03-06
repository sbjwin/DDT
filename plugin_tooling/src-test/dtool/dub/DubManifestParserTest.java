/*******************************************************************************
 * Copyright (c) 2013, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package dtool.dub;

import java.nio.file.Path;

import melnorme.utilbox.misc.Location;

import org.junit.Test;

import dtool.tests.DToolTestResources;

public class DubManifestParserTest extends CommonDubTest {
	
	@Test
	public void testBasic() throws Exception { testBasic$(); }
	public void testBasic$() throws Exception {
		
		testBundle(main(DUB_TEST_BUNDLES.resolve_fromValid("bar_lib"), 
			null, "bar_lib", DubBundle.DEFAULT_VERSION, paths("source"),
			rawDeps()));
		
		testBundle(main(DUB_TEST_BUNDLES.resolve_fromValid("foo_lib"), 
			null, "foo_lib", DubBundle.DEFAULT_VERSION, paths("src", "src2"), 
			rawDeps("bar_lib")));
		
		testBundle(main(DUB_TEST_BUNDLES.resolve_fromValid("XptoBundle"), 
			null, "xptobundle", DubBundle.DEFAULT_VERSION, paths("src", "src-test", "src-import"),
			rawDeps("foo_lib")));
		
		testBundle(main(DUB_TEST_BUNDLES.resolve_fromValid("LenientJsonA"), 
			null, "lenient-json1", DubBundle.DEFAULT_VERSION, paths("src", "src-test"),
			rawDeps("foo_lib", "other_lib")));
		
		
		testPath(DUB_TEST_BUNDLES.resolve_fromValid("XptoBundle"), 
			"bin", path("bin/xptobundle" + DubBundle.getExecutableSuffix()));

		testPath(DUB_TEST_BUNDLES.resolve_fromValid("bar_lib"), 
			null, path("bar_lib" + DubBundle.getExecutableSuffix()));
	}
	
	protected void testPath(Location location, String targetPath, Path expectedEffectiveFullPath) {
		DubBundle xptoBundle = parseDubBundle(location);
		assertAreEqual(xptoBundle.getTargetPath(), targetPath);
		assertAreEqual(xptoBundle.getEffectiveTargetFullPath(), expectedEffectiveFullPath);
	}
	
	public DubBundle parseDubBundle(Location location) {
		return DubManifestParser.parseDubBundleFromLocation(BundlePath.create(location));
	}
	
	public void testBundle(DubBundleChecker bundle) {
		bundle.check(parseDubBundle(bundle.location), false);
	}
	
	
	@Test
	public void testNonExistant() throws Exception { testNonExistant$(); }
	public void testNonExistant$() throws Exception {
		testBundle(bundle(DToolTestResources.getTestResourceLoc("dub").resolve_fromValid("nonExistent"), 
			"java.io.FileNotFoundException", IGNORE_STR, IGNORE_STR, null));
	}
	
}