package au.org.ala.names.ws.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SpeciesGroupTest {

    @Test
    public void isPartOfGroup1() {
        SpeciesGroup group = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build()
                ))
                .build();
        assertTrue(group.isPartOfGroup(200));
        assertTrue(group.isPartOfGroup(100));
        assertTrue(group.isPartOfGroup(300));
        assertFalse(group.isPartOfGroup(500));
    }

    @Test
    public void isPartOfGroup2() {
        SpeciesGroup group = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(250).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group.isPartOfGroup(200));
        assertTrue(group.isPartOfGroup(100));
        assertTrue(group.isPartOfGroup(300));
        assertFalse(group.isPartOfGroup(500));
    }

    @Test
    public void isPartOfGroup3() {
        SpeciesGroup group = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(250).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build(),
                        LftRgtValues.builder().lft(450).rgt(500).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group.isPartOfGroup(200));
        assertTrue(group.isPartOfGroup(100));
        assertTrue(group.isPartOfGroup(300));
        assertFalse(group.isPartOfGroup(425));
        assertFalse(group.isPartOfGroup(500));
    }

    @Test
    public void isPartOfGroup4() {
        SpeciesGroup group = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(250).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(425).rgt(500).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build(),
                        LftRgtValues.builder().lft(450).rgt(600).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group.isPartOfGroup(200));
        assertTrue(group.isPartOfGroup(100));
        assertTrue(group.isPartOfGroup(300));
        assertFalse(group.isPartOfGroup(425));
        assertTrue(group.isPartOfGroup(500));
    }


    @Test
    public void overlaps1() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build()
                ))
                .build();
        assertTrue(group1.overlaps(group1));
    }

    @Test
    public void overlaps2() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(400).rgt(500).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group1.overlaps(group2));
        assertFalse(group2.overlaps(group1));
    }


    @Test
    public void overlaps3() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(300).rgt(500).tobeIncluded(true).build()
                ))
                .build();
        assertTrue(group1.overlaps(group2));
        assertTrue(group2.overlaps(group1));
    }


    @Test
    public void overlaps4() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(100).rgt(400).tobeIncluded(true).build(),
                        LftRgtValues.builder().lft(500).rgt(600).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(400).rgt(500).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group1.overlaps(group2));
        assertFalse(group2.overlaps(group1));
    }


    @Test
    public void overlaps5() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(200).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(100).rgt(300).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(200).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group1.overlaps(group2));
        assertFalse(group2.overlaps(group1));
    }


    @Test
    public void overlaps6() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(200).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(100).rgt(300).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(170).rgt(250).tobeIncluded(true).build()
                ))
                .build();
        assertTrue(group1.overlaps(group2));
        assertTrue(group2.overlaps(group1));
    }

    @Test
    public void overlaps7() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(150).rgt(200).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(100).rgt(300).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(170).rgt(190).tobeIncluded(false).build(),
                        LftRgtValues.builder().lft(150).rgt(200).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group1.overlaps(group2));
        assertFalse(group2.overlaps(group1));
    }


    @Test
    public void overlaps8() {
        SpeciesGroup group1 = SpeciesGroup.builder()
                .name("Test 1")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(100).rgt(200).tobeIncluded(true).build(),
                        LftRgtValues.builder().lft(400).rgt(500).tobeIncluded(true).build()
                ))
                .build();
        SpeciesGroup group2 = SpeciesGroup.builder()
                .name("Test 2")
                .rank("species")
                .parent("Animals")
                .lftRgtValues(Arrays.asList(
                        LftRgtValues.builder().lft(50).rgt(90).tobeIncluded(true).build(),
                        LftRgtValues.builder().lft(300).rgt(350).tobeIncluded(true).build()
                ))
                .build();
        assertFalse(group1.overlaps(group2));
        assertFalse(group2.overlaps(group1));
    }

}