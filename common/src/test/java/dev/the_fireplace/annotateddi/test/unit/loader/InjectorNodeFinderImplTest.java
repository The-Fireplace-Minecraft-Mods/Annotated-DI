package dev.the_fireplace.annotateddi.test.unit.loader;

import com.google.common.collect.Sets;
import dev.the_fireplace.annotateddi.impl.loader.InjectorNodeFinderImpl;
import dev.the_fireplace.annotateddi.test.stub.LoaderHelperStub;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public final class InjectorNodeFinderImplTest
{
    private final LoaderHelperStub loaderHelperStub;

    public InjectorNodeFinderImplTest() {
        loaderHelperStub = new LoaderHelperStub();
    }

    private InjectorNodeFinderImpl createInjectorNodeFinder() {
        return new InjectorNodeFinderImpl(loaderHelperStub);
    }

    @Test
    public void test_getNode_simpleSetup_findsNodeContainingGivenModid() {
        // Arrange
        String modId = "testmod";
        loaderHelperStub.addMod(modId, Collections.emptySet());

        // Act
        Collection<String> actual = createInjectorNodeFinder().getNode(modId);

        // Assert
        assertEquals(1, actual.size());
        assertTrue(actual.contains(modId));
    }

    @Test
    public void test_getNode_codependentMods_findsNodeContainingBothMods() {
        // Arrange
        String modId = "testmod";
        String modId2 = "testmod2";
        loaderHelperStub.addMod(modId, Sets.newHashSet(modId2));
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getNode(modId);

        // Assert
        assertEquals(2, actual.size());
        assertTrue(actual.contains(modId));
        assertTrue(actual.contains(modId2));
    }

    @Test
    public void test_getNode_dependencyLoop_findsNodeContainingAllMods() {
        // Arrange
        String modId = "testmod";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        String modId5 = "testmod5";
        loaderHelperStub.addMod(modId, Sets.newHashSet(modId2));
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId3));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId4));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId5));
        loaderHelperStub.addMod(modId5, Sets.newHashSet(modId));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getNode(modId);

        // Assert
        assertEquals(5, actual.size());
        assertTrue(actual.contains(modId));
        assertTrue(actual.contains(modId2));
        assertTrue(actual.contains(modId3));
        assertTrue(actual.contains(modId4));
        assertTrue(actual.contains(modId5));
    }

    @Test
    public void test_getNode_dependencyLoop_otherModsPresent_findsNodeContainingOnlyCodependentMods() {
        // Arrange
        String modId = "testmod";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        String modId5 = "testmod5";
        String otherModId1 = "othertestmod1";
        String otherModId2 = "othertestmod2";
        loaderHelperStub.addMod(modId, Sets.newHashSet(modId2));
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId3));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId4));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId5));
        loaderHelperStub.addMod(modId5, Sets.newHashSet(modId));
        loaderHelperStub.addMod(otherModId1, Collections.emptySet());
        loaderHelperStub.addMod(otherModId2, Sets.newHashSet(otherModId1));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getNode(modId);

        // Assert
        assertEquals(5, actual.size());
        assertTrue(actual.contains(modId));
        assertTrue(actual.contains(modId2));
        assertTrue(actual.contains(modId3));
        assertTrue(actual.contains(modId4));
        assertTrue(actual.contains(modId5));
    }

    @Test
    public void test_getNode_notCodependent_withParent_dependencyLoopPresentInTree_findsNodeContainingOnlySelf() {
        // Arrange
        String modId = "testmod";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        String modId5 = "testmod5";
        String otherModId1 = "othertestmod1";
        String otherModId2 = "othertestmod2";
        loaderHelperStub.addMod(modId, Sets.newHashSet(modId2));
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId3));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId4));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId5));
        loaderHelperStub.addMod(modId5, Sets.newHashSet(modId));
        loaderHelperStub.addMod(otherModId1, Sets.newHashSet(modId4));
        loaderHelperStub.addMod(otherModId2, Sets.newHashSet(otherModId1));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getNode(otherModId2);

        // Assert
        assertEquals(1, actual.size());
        assertTrue(actual.contains(otherModId2));
    }

    @Test
    public void test_getParent_isMinecraft_returnsNull() {
        // Arrange
        String modId = "minecraft";
        loaderHelperStub.addMod(modId, Collections.emptySet());

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId);

        // Assert
        assertNull(actual);
    }

    @Test
    public void test_getParent_noDependenciesDeclared_returnsMinecraft() {
        // Arrange
        String modId = "not_minecraft";
        loaderHelperStub.addMod(modId, Collections.emptySet());

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertTrue(actual.contains("minecraft"));
    }

    @Test
    public void test_getParent_simpleParent_returnsParentNode() {
        // Arrange
        String modId = "testmod";
        String modId2 = "testmod2";
        loaderHelperStub.addMod(modId, Collections.emptySet());
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId2);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertTrue(actual.contains(modId));
    }

    @Test
    public void test_getParent_simpleChain_returnsParentNode() {
        // Arrange
        String modId = "testmod";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        loaderHelperStub.addMod(modId, Collections.emptySet());
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId2));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId3));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId3);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertTrue(actual.contains(modId2));
    }

    @Test
    public void test_getParent_missingSoftDependency_returnsParentNode() {
        // Arrange
        String modId = "testmod";
        String missingModId = "some soft dependency";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        loaderHelperStub.addMod(modId, Collections.emptySet());
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId, missingModId));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId2));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId3));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId3);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertTrue(actual.contains(modId2));
    }

    @Test
    public void test_getParent_multipleModsMissingCommonSoftDependency_returnsParentNode() {
        // Arrange
        String modId = "testmod";
        String missingModId = "some soft dependency";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        loaderHelperStub.addMod(modId, Collections.emptySet());
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId, missingModId));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId2, missingModId));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId3));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId4);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertTrue(actual.contains(modId3));
    }

    @Test
    public void test_getParent_multipleModsMissingCommonSoftDependency_withDependencyLoop_returnsParentNode() {
        // Arrange
        String modId = "testmod";
        String missingModId = "some soft dependency";
        String modId2 = "testmod2";
        String modId3 = "testmod3";
        String modId4 = "testmod4";
        loaderHelperStub.addMod(modId, Sets.newHashSet(modId3));
        loaderHelperStub.addMod(modId2, Sets.newHashSet(modId, missingModId));
        loaderHelperStub.addMod(modId3, Sets.newHashSet(modId2, missingModId));
        loaderHelperStub.addMod(modId4, Sets.newHashSet(modId3));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId4);

        // Assert
        assertNotNull(actual);
        assertEquals(3, actual.size());
        assertTrue(actual.contains(modId));
        assertTrue(actual.contains(modId2));
        assertTrue(actual.contains(modId3));
    }

    @Test
    public void test_getParent_nonChainedDependencies_returnsParentNode() {
        // Arrange
        String someRoot = "someRoot";
        String branch1 = "branch1";
        String branch2 = "branch2";
        String commonLeaf = "commonLeaf";
        loaderHelperStub.addMod(someRoot, Collections.emptySet());
        loaderHelperStub.addMod(branch1, Sets.newHashSet(someRoot));
        loaderHelperStub.addMod(branch2, Sets.newHashSet(someRoot));
        loaderHelperStub.addMod(commonLeaf, Sets.newHashSet(branch1, branch2));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(commonLeaf);

        // Assert
        assertNotNull(actual);
        assertEquals(1, actual.size());
    }

    @Test
    public void test_getParent_nonChainedDependencies_withCodependentDependency_returnsParentNode() {
        // Arrange
        String someRoot = "someRoot";
        String branch1 = "branch1";
        String codependent1 = "codependent1";
        String codependent2 = "codependent2";
        String commonLeaf = "commonLeaf";
        loaderHelperStub.addMod(someRoot, Collections.emptySet());
        loaderHelperStub.addMod(branch1, Sets.newHashSet(someRoot));
        loaderHelperStub.addMod(codependent1, Sets.newHashSet(someRoot, codependent2));
        loaderHelperStub.addMod(codependent2, Sets.newHashSet(someRoot, codependent1));
        loaderHelperStub.addMod(commonLeaf, Sets.newHashSet(branch1, codependent1));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(commonLeaf);

        // Assert
        assertNotNull(actual);
    }
}
