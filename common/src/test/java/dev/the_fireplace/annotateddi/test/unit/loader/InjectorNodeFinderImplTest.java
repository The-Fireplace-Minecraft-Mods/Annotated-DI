package dev.the_fireplace.annotateddi.test.unit.loader;

import dev.the_fireplace.annotateddi.impl.loader.InjectorNodeFinderImpl;
import dev.the_fireplace.annotateddi.impl.loader.InjectorTreeBuilder;
import dev.the_fireplace.annotateddi.test.stub.LoaderHelperStub;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class InjectorNodeFinderImplTest
{
    private final LoaderHelperStub loaderHelperStub;

    public InjectorNodeFinderImplTest() {
        loaderHelperStub = new LoaderHelperStub();
    }

    private InjectorNodeFinderImpl createInjectorNodeFinder() {
        return new InjectorNodeFinderImpl(new InjectorTreeBuilder(loaderHelperStub));
    }

    @Test
    public void test_getNode_simpleSetup_findsNodeContainingGivenModid() {
        // Arrange
        String modId = "testmod";
        loaderHelperStub.addMod(modId, Set.of());

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
        loaderHelperStub.addMod(modId, Set.of(modId2));
        loaderHelperStub.addMod(modId2, Set.of(modId));

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
        loaderHelperStub.addMod(modId, Set.of(modId2));
        loaderHelperStub.addMod(modId2, Set.of(modId3));
        loaderHelperStub.addMod(modId3, Set.of(modId4));
        loaderHelperStub.addMod(modId4, Set.of(modId5));
        loaderHelperStub.addMod(modId5, Set.of(modId));

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
        loaderHelperStub.addMod(modId, Set.of(modId2));
        loaderHelperStub.addMod(modId2, Set.of(modId3));
        loaderHelperStub.addMod(modId3, Set.of(modId4));
        loaderHelperStub.addMod(modId4, Set.of(modId5));
        loaderHelperStub.addMod(modId5, Set.of(modId));
        loaderHelperStub.addMod(otherModId1, Set.of());
        loaderHelperStub.addMod(otherModId2, Set.of(otherModId1));

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
        loaderHelperStub.addMod(modId, Set.of(modId2));
        loaderHelperStub.addMod(modId2, Set.of(modId3));
        loaderHelperStub.addMod(modId3, Set.of(modId4));
        loaderHelperStub.addMod(modId4, Set.of(modId5));
        loaderHelperStub.addMod(modId5, Set.of(modId));
        loaderHelperStub.addMod(otherModId1, Set.of(modId4));
        loaderHelperStub.addMod(otherModId2, Set.of(otherModId1));

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
        loaderHelperStub.addMod(modId, Set.of());

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(modId);

        // Assert
        assertNull(actual);
    }

    @Test
    public void test_getParent_noDependenciesDeclared_returnsMinecraft() {
        // Arrange
        String modId = "not_minecraft";
        loaderHelperStub.addMod(modId, Set.of());

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
        loaderHelperStub.addMod(modId, Set.of());
        loaderHelperStub.addMod(modId2, Set.of(modId));

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
        loaderHelperStub.addMod(modId, Set.of());
        loaderHelperStub.addMod(modId2, Set.of(modId));
        loaderHelperStub.addMod(modId3, Set.of(modId2));
        loaderHelperStub.addMod(modId4, Set.of(modId3));

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
        loaderHelperStub.addMod(modId, Set.of());
        loaderHelperStub.addMod(modId2, Set.of(modId, missingModId));
        loaderHelperStub.addMod(modId3, Set.of(modId2));
        loaderHelperStub.addMod(modId4, Set.of(modId3));

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
        loaderHelperStub.addMod(modId, Set.of());
        loaderHelperStub.addMod(modId2, Set.of(modId, missingModId));
        loaderHelperStub.addMod(modId3, Set.of(modId2, missingModId));
        loaderHelperStub.addMod(modId4, Set.of(modId3));

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
        loaderHelperStub.addMod(modId, Set.of(modId3));
        loaderHelperStub.addMod(modId2, Set.of(modId, missingModId));
        loaderHelperStub.addMod(modId3, Set.of(modId2, missingModId));
        loaderHelperStub.addMod(modId4, Set.of(modId3));

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
        loaderHelperStub.addMod(someRoot, Set.of());
        loaderHelperStub.addMod(branch1, Set.of(someRoot));
        loaderHelperStub.addMod(branch2, Set.of(someRoot));
        loaderHelperStub.addMod(commonLeaf, Set.of(branch1, branch2));

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
        loaderHelperStub.addMod(someRoot, Set.of());
        loaderHelperStub.addMod(branch1, Set.of(someRoot));
        loaderHelperStub.addMod(codependent1, Set.of(someRoot, codependent2));
        loaderHelperStub.addMod(codependent2, Set.of(someRoot, codependent1));
        loaderHelperStub.addMod(commonLeaf, Set.of(branch1, codependent1));

        // Act
        Collection<String> actual = createInjectorNodeFinder().getParentNode(commonLeaf);

        // Assert
        assertNotNull(actual);
    }
}
