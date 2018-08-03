package com.hp.jipp.encoding;

import com.hp.jipp.pwg.OperationGroup;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.Assert.*;

import static com.hp.jipp.encoding.Cycler.*;

import kotlin.text.Charsets;

public class AttributeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void octetString() throws Exception {
        AttributeType<byte[]> octetsType = new OctetsType("name");
        Attribute<byte[]> attribute = octetsType.of("value".getBytes(Charsets.UTF_8));
        assertArrayEquals(new byte[] {
                (byte)0x30, // octetString
                (byte)0x00,
                (byte)0x04,
                'n', 'a', 'm', 'e',
                (byte)0x00,
                (byte)0x05,
                'v', 'a', 'l', 'u', 'e'
        }, toBytes(attribute));
        attribute = cycle(attribute);
        assertNull(attribute.getTag());
        assertEquals("name", attribute.getName());
        assertArrayEquals("value".getBytes(Charsets.UTF_8), (byte[]) attribute.get(0));
    }

    @Test
    public void parseOtherOctet() throws Exception {
        byte[] bytes = new byte[] {
                (byte)0x39, // Reserved octetString type
                (byte)0x00,
                (byte)0x04,
                'n', 'a', 'm', 'e',
                (byte)0x00,
                (byte)0x05,
                'v', 'a', 'l', 'u', 'e'
        };
        Attribute<?> attribute = AttributeGroup.Companion.readNextAttribute(new IppInputStream(new ByteArrayInputStream(bytes)));
        assertEquals("name", attribute.getName());
        OtherOctets expected = new OtherOctets(Tag.fromInt(0x39), "value".getBytes(Charsets.UTF_8));
        assertEquals(OtherOctets.class, attribute.getValue().getClass());
        assertArrayEquals("value".getBytes(Charsets.UTF_8), ((OtherOctets)attribute.getValue()).getValue());
        assertEquals(expected, attribute.getValue());
        assertEquals(expected.hashCode(), attribute.getValue().hashCode());
        assertTrue(attribute.getValue().toString().contains("76616c7565"));
        assertTrue(attribute.getValue().toString().contains("tag(x39)"));
    }

    @Test
    public void equality() throws Exception {
        Attribute<String> stringAttr = OperationGroup.attributesCharset.of("one", "two", "three");
        // equals must be symmetric:
        assertEquals(Arrays.asList("one", "two", "three"), stringAttr);
        assertEquals(stringAttr, Arrays.asList("one", "two", "three"));
        assertEquals(Arrays.asList("one", "two", "three").hashCode(), stringAttr.hashCode());
    }

    //
//    @Test
//    public void multiOctetString() throws IOException {
//        AttributeType<byte[]> stringType = new OctetStringType(Tag.nameWithoutLanguage, "name");
//        Attribute<byte[]> attribute = stringType.of("value".getBytes(Charsets.UTF_8), "value2".getBytes(Charsets.UTF_8));
//        assertEquals(2, attribute.size());
//        assertArrayEquals("value".getBytes(Charsets.UTF_8), attribute.get(0));
//        assertArrayEquals("value2".getBytes(Charsets.UTF_8), attribute.get(1));
//    }
//
//    @Test
//    public void multiOctetStringIterate() throws IOException {
//        AttributeType<byte[]> stringType = new OctetStringType(Tag.nameWithoutLanguage, "name");
//        Attribute<byte[]> attribute = stringType.of("value".getBytes(Charsets.UTF_8), "value2".getBytes(Charsets.UTF_8));
//        List<byte[]> list = new ArrayList<byte[]>(attribute);
//        assertEquals(2, list.size());
//        assertArrayEquals("value".getBytes(Charsets.UTF_8), list.get(0));
//        assertArrayEquals("value2".getBytes(Charsets.UTF_8), list.get(1));
//    }
//
//    @Test
//    public void multiBoolean() throws IOException {
//        AttributeType<Boolean> booleanType = new BooleanType("name");
//        Attribute<Boolean> attribute = cycle(booleanType.of(true, false));
//        assertEquals(Arrays.asList(true, false), attribute.getValues());
//    }
//
//    @Test
//    public void multiInteger() throws IOException {
//        AttributeType<Integer> integerType = new IntType(Tag.integerValue, "name");
//        Attribute<Integer> attribute = cycle(integerType.of(-50505, 50505));
//        assertEquals(Arrays.asList(-50505, 50505), attribute.getValues());
//    }
//
//    @Test
//    public void enumAttribute() throws IOException {
//        AttributeGroup group = Cycler.cycle(groupOf(Tag.printerAttributes,
//                Types.operationsSupported.of(
//                        Operation.cancelJob, Operation.getJobAttributes,
//                        Operation.createJob)));
//        assertEquals(Arrays.asList(Operation.cancelJob, Operation.getJobAttributes,
//                Operation.createJob),
//                group.getValues(Types.operationsSupported));
//    }
//
//    @Test
//    public void surpriseEnum() throws IOException {
//        AttributeGroup group = Cycler.cycle(groupOf(Tag.printerAttributes,
//                Types.operationsSupported.of(
//                        new Operation(0x4040, "vendor-specific"))));
//        // We can't know it's called "vendor-specific" after parsing, since we just made it up.
//        // So expect the unrecognized format
//        assertEquals(Arrays.asList(new Operation(0x4040, "Unknown Operation")),
//                group.getValues(Types.operationsSupported));
//    }
//
//    @Test
//    public void invalidTag() {
//        exception.expect(BuildError.class);
//        // String is not Integer; should throw.
//        new StringType(Tag.integerValue, "something");
//    }
//
//    @Test
//    public void missingEncoder() throws Exception {
//        exception.expect(ParseError.class);
//        byte[] bytes = new byte[] {
//                0, 2, 'x', 0,
//                1,
//                0
//        };
//        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes), new Encoder.Finder() {
//            @Override
//            @NotNull
//            public Encoder<?> find(@NotNull Tag valueTag, @NotNull String name) throws IOException {
//                throw new ParseError("");
//            }
//        });
//
//        input.readAttribute(Tag.octetString);
//    }
//
//    @Test
//    public void insufficientLength() throws Exception {
//        exception.expect(ParseError.class);
//        exception.expectMessage("Bad attribute length: expected 4, got 1");
//        byte[] bytes = new byte[] {
//                0,
//                0,
//                0,
//                1,
//                0
//        };
//        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes), Cycler.sFinder);
//        input.readAttribute(Tag.integerValue);
//    }
//
//    @Test
//    public void badTag() throws Exception {
//        exception.expect(BuildError.class);
//        exception.expectMessage("Invalid tag tag(x77) for Integer");
//        new Attribute<Integer>(Tag.fromInt(0x77), "", Collections.singletonList(5), IntType.Encoder);
//    }
//
//    @Test
//    public void tagNames() throws Exception {
//        assertEquals("Integer", IntType.Encoder.getTypeName());
//        assertEquals("octetString", OctetStringType.Encoder.getTypeName());
//        assertEquals("rangeOfInteger", IntRangeType.Encoder.getTypeName());
//        assertEquals("resolution", ResolutionType.Encoder.getTypeName());
//        assertEquals("Name", NameType.Encoder.getTypeName());
//        assertEquals("Text", TextType.Encoder.getTypeName());
//        assertEquals("Integer", IntType.Encoder.getTypeName());
//        assertEquals("URI", UriType.Encoder.getTypeName());
//        assertEquals("Collection", CollectionType.Encoder.getTypeName());
//        assertEquals("Boolean", BooleanType.Encoder.getTypeName());
//        assertEquals("String", StringType.Encoder.getTypeName());
//        assertEquals("Status", Status.Encoder.getTypeName());
//    }
//
//    @Test
//    public void resolutionUnits() throws Exception {
//        byte[] bytes = new byte[] {
//                0,
//                9,
//                0,
//                0,
//                1,
//                0,
//                0,
//                0,
//                2,
//                0,
//                5,
//        };
//
//        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes), Types.allFinder);
//        Resolution resolution = ResolutionType.Encoder.readValue(input, Tag.resolution);
//        assertEquals("256x512 Unknown ResolutionUnit(5)", resolution.toString());
//
//        KotlinTest.cover(resolution,
//                resolution.copy(resolution.component1(), resolution.component2(), resolution.component3()),
//                resolution.copy(777, resolution.component2(), resolution.component3()));
//
//        ResolutionUnit unit = resolution.getUnit();
//        KotlinTest.cover(unit, unit.copy(unit.component1(), unit.component2()),
//                unit.copy(unit.component1(), "copy"));
//    }
//
//    @Test
//    public void shortRead() throws IOException {
//        exception.expect(ParseError.class);
//        exception.expectMessage("Value too short: expected 2 but got 1");
//        byte[] bytes = new byte[] {
//                0,
//                2,
//                0,
//        };
//        IppInputStream input = new IppInputStream(new ByteArrayInputStream(bytes), Types.allFinder);
//        input.readValueBytes();
//    }
//
//    @Test
//    public void badConversion() throws IOException {
//        assertNull(Types.jobId.convert(Types.jobName.of("string")));
//    }
//
//    @Test
//    public void goodConversion() throws IOException {
//        assertEquals(Types.jobId.of(1),
//                Types.jobId.of(new IntType(Tag.integerValue, "job-id").of(1)));
//    }
//
//    @Test
//    public void printBinary() throws Exception {
//        System.out.println(new OctetStringType(Tag.octetString, "data").of(new byte[] { 1, 2, 3 }).toString());
//        assertTrue(new OctetStringType(Tag.octetString, "data").of(new byte[] { 1, 2, 3 }).toString().contains("x010203"));
//    }
//
//    @Test
//    public void cover() throws IOException {
//        Attribute<Name> jobName = Types.jobName.of("hello");
//        KotlinTest.cover(jobName,
//                jobName.copy(jobName.component1(), jobName.component2(), jobName.component3(), jobName.component4()),
//                jobName.copy(jobName.component1(), "goodbye", jobName.component3(), jobName.component4()));
//    }
}
