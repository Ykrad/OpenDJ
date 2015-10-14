/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2006-2010 Sun Microsystems, Inc.
 *      Portions Copyright 2014-2015 ForgeRock AS
 */
package org.opends.server.types;

import static org.opends.server.TestCaseUtils.*;
import static org.opends.server.core.DirectoryServer.*;
import static org.testng.Assert.*;

import java.util.ArrayList;

import org.forgerock.opendj.ldap.ByteString;
import org.opends.server.TestCaseUtils;
import org.opends.server.core.DirectoryServer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class defines a set of tests for the
 * {@link org.opends.server.types.RDN} class.
 */
public final class TestRDN extends TypesTestCase {

  /** Domain component attribute type. */
  private AttributeType AT_DC;
  /** Common name attribute type. */
  private AttributeType AT_CN;

  /** Test attribute value. */
  private ByteString AV_DC_ORG;
  /** Test attribute value. */
  private ByteString AV_CN;


  /**
   * Set up the environment for performing the tests in this suite.
   *
   * @throws Exception
   *           If the environment could not be set up.
   */
  @BeforeClass
  public void setUp() throws Exception {
    // This test suite depends on having the schema available, so
    // we'll start the server.
    TestCaseUtils.startServer();

    AT_DC = DirectoryServer.getAttributeTypeOrNull("dc");
    AT_CN = DirectoryServer.getAttributeTypeOrNull("cn");

    String attrName = "x-test-integer-type";
    AttributeType dummy = getAttributeTypeOrDefault(attrName, attrName, getDefaultIntegerSyntax());
    DirectoryServer.getSchema().registerAttributeType(dummy, true);

    AV_DC_ORG = ByteString.valueOf("org");
    AV_CN = ByteString.valueOf("hello world");
  }



  /**
   * Test RDN construction with single AVA.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testConstructor() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);

    assertEquals(rdn.getNumValues(), 1);
    assertEquals(rdn.getAttributeType(0), AT_DC);
    assertEquals(rdn.getAttributeName(0), AT_DC.getNameOrOID());
    assertEquals(rdn.getAttributeValue(0), AV_DC_ORG);
  }



  /**
   * Test RDN construction with single AVA and a user-defined name.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testConstructorWithName() throws Exception {
    RDN rdn = new RDN(AT_DC, "domainComponent", AV_DC_ORG);

    assertEquals(rdn.getNumValues(), 1);
    assertEquals(rdn.getAttributeType(0), AT_DC);
    assertEquals(rdn.getAttributeName(0), "domainComponent");
    assertEquals(rdn.getAttributeValue(0), AV_DC_ORG);
  }



  /**
   * Test RDN construction with a multiple AVA elements.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testConstructorMultiAVA() throws Exception {
    AttributeType[]  attrTypes  = { AT_DC, AT_CN };
    String[]         attrNames  = { AT_DC.getNameOrOID(),
                                    AT_CN.getNameOrOID() };
    ByteString[]     attrValues = { AV_DC_ORG, AV_CN };

    RDN rdn = new RDN(attrTypes, attrNames, attrValues);

    assertEquals(rdn.getNumValues(), 2);

    assertEquals(rdn.getAttributeType(0), AT_DC);
    assertEquals(rdn.getAttributeName(0), AT_DC.getNameOrOID());
    assertEquals(rdn.getAttributeValue(0), AV_DC_ORG);

    assertEquals(rdn.getAttributeType(1), AT_CN);
    assertEquals(rdn.getAttributeName(1), AT_CN.getNameOrOID());
    assertEquals(rdn.getAttributeValue(1), AV_CN);
  }



  /**
   * Test RDN construction with a multiple AVA elements.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testConstructorMultiAVAList() throws Exception {
    ArrayList<AttributeType>  typeList  = new ArrayList<>();
    ArrayList<String>         nameList  = new ArrayList<>();
    ArrayList<ByteString>     valueList = new ArrayList<>();

    typeList.add(AT_DC);
    nameList.add(AT_DC.getNameOrOID());
    valueList.add(AV_DC_ORG);

    typeList.add(AT_CN);
    nameList.add(AT_CN.getNameOrOID());
    valueList.add(AV_CN);

    RDN rdn = new RDN(typeList, nameList, valueList);

    assertEquals(rdn.getNumValues(), 2);

    assertEquals(rdn.getAttributeType(0), AT_DC);
    assertEquals(rdn.getAttributeName(0), AT_DC.getNameOrOID());
    assertEquals(rdn.getAttributeValue(0), AV_DC_ORG);

    assertEquals(rdn.getAttributeType(1), AT_CN);
    assertEquals(rdn.getAttributeName(1), AT_CN.getNameOrOID());
    assertEquals(rdn.getAttributeValue(1), AV_CN);
  }



  /**
   * Test escaping of single space values.
   *
   * @throws Exception  If the test failed unexpectedly.
   */
  @Test
  public void testEscaping() {
    RDN rdn = new RDN(AT_DC, ByteString.valueOf(" "));
    assertEquals(rdn.toString(), "dc=\\ ");
  }



  /**
   * RDN test data provider.
   *
   * @return The array of test RDN strings.
   */
  @DataProvider(name = "testRDNs")
  public Object[][] createData() {
    return new Object[][] {
        { "dc=hello world", "dc=hello%20world", "dc=hello world" },
        { "dc =hello world", "dc=hello%20world", "dc=hello world" },
        { "dc  =hello world", "dc=hello%20world", "dc=hello world" },
        { "dc= hello world", "dc=hello%20world", "dc=hello world" },
        { "dc=  hello world", "dc=hello%20world", "dc=hello world" },
        { "undefined=hello", "undefined=hello", "undefined=hello" },
        { "DC=HELLO WORLD", "dc=hello%20world", "DC=HELLO WORLD" },
        { "dc = hello    world", "dc=hello%20world", "dc=hello    world" },
        { "   dc = hello world   ", "dc=hello%20world",  "dc=hello world" },
        { "givenName=John+cn=Doe", "cn=doe+givenname=john", "givenName=John+cn=Doe" },
        { "givenName=John\\+cn=Doe", "givenname=john%2Bcn%3Ddoe", "givenName=John\\+cn=Doe" },
        { "cn=Doe\\, John", "cn=doe%2C%20john", "cn=Doe\\, John" },
        { "OU=Sales+CN=J. Smith", "cn=j.%20smith+ou=sales","OU=Sales+CN=J. Smith" },
        { "CN=James \\\"Jim\\\" Smith\\, III", "cn=james%20%22jim%22%20smith%2C%20iii",
            "CN=James \\\"Jim\\\" Smith\\, III" },
            //\0d is a hex representation of Carriage return. It is mapped
             //to a SPACE as defined in the MAP ( RFC 4518)
        { "CN=Before\\0dAfter", "cn=before%20after", "CN=Before\\0dAfter" },
        { "1.3.6.1.4.1.1466.0=#04024869",
            //Unicode codepoints from 0000-0008 are mapped to nothing.
            "1.3.6.1.4.1.1466.0=hi", "1.3.6.1.4.1.1466.0=\\04\\02Hi" },
        { "CN=Lu\\C4\\8Di\\C4\\87", "cn=luc%CC%8Cic%CC%81", "CN=Lu\u010di\u0107" },
        { "ou=\\e5\\96\\b6\\e6\\a5\\ad\\e9\\83\\a8", "ou=%E5%96%B6%E6%A5%AD%E9%83%A8", "ou=\u55b6\u696d\u90e8" },
        { "photo=\\ john \\ ", "photo=%20john%20%20", "photo=\\ john \\ " },
     //   { "AB-global=", "ab-global=", "AB-global=" },
        { "cn=John+a=", "a=+cn=john", "cn=John+a=" },
        { "OID.1.3.6.1.4.1.1466.0=#04024869",
            //Unicode codepoints from 0000-0008 are mapped to nothing.
            "1.3.6.1.4.1.1466.0=hi",
            "1.3.6.1.4.1.1466.0=\\04\\02Hi" },
        { "O=\"Sue, Grabbit and Runn\"", "o=sue%2C%20grabbit%20and%20runn", "O=Sue\\, Grabbit and Runn" }, };
  }



  /**
   * Test RDN string decoder.
   *
   * @param rawRDN
   *          Raw RDN string representation.
   * @param normRDN
   *          Normalized RDN string representation.
   * @param stringRDN
   *          String representation.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "testRDNs")
  public void testNormalizationToSafeUrlString(String rawRDN, String normRDN, String stringRDN) throws Exception {
    RDN rdn = RDN.decode(rawRDN);
    assertEquals(rdn.toNormalizedUrlSafeString(), normRDN);
  }



  /**
   * Illegal RDN test data provider.
   *
   * @return The array of illegal test RDN strings.
   */
  @DataProvider(name = "illegalRDNs")
  public Object[][] createIllegalData() {
    return new Object[][] { { null }, { "" }, { " " }, { "=" }, { "manager" },
        { "manager " }, { "cn+"}, { "cn+Jim" }, { "cn=Jim+" }, { "cn=Jim +" },
        { "cn=Jim+ " }, { "cn=Jim+sn" }, { "cn=Jim+sn " },
        { "cn=Jim+sn equals" }, { "cn=Jim," }, { "cn=Jim;" }, { "cn=Jim,  " },
        { "cn=Jim+sn=a," }, { "cn=Jim, sn=Jam " }, { "cn+uid=Jim" },
        { "-cn=Jim" }, { "/tmp=a" }, { "\\tmp=a" }, { "cn;lang-en=Jim" },
        { "@cn=Jim" }, { "_name_=Jim" }, { "\u03c0=pi" }, { "v1.0=buggy" },
        { "cn=Jim+sn=Bob++" }, { "cn=Jim+sn=Bob+," },
        { "1.3.6.1.4.1.1466..0=#04024869" }, };
  }



  /**
   * Test RDN string decoder against illegal strings.
   *
   * @param rawRDN
   *          Illegal RDN string representation.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "illegalRDNs", expectedExceptions = DirectoryException.class)
  public void testDecodeString(String rawRDN) throws Exception {
    RDN.decode(rawRDN);

    fail("Expected exception for value \"" + rawRDN + "\"");
  }



  /**
   * Test getAttributeName.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetAttributeName() throws Exception {
    AttributeType[]  attrTypes  = { AT_DC, AT_CN };
    String[]         attrNames  = { AT_DC.getNameOrOID(),
                                    AT_CN.getNameOrOID() };
    ByteString[]     attrValues = { AV_DC_ORG, AV_CN };

    RDN rdn = new RDN(attrTypes, attrNames, attrValues);

    assertEquals(rdn.getAttributeName(0), AT_DC.getNameOrOID());
    assertEquals(rdn.getAttributeName(1), AT_CN.getNameOrOID());
  }


  @SuppressWarnings("javadoc")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ensureRDNIsCreatedWithNonEmptyArguments() throws Exception {
      new RDN(new AttributeType[0], new String[0], new ByteString[0]);
  }

  /**
   * Test getAttributeType.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetAttributeType() throws Exception {
    AttributeType[]  attrTypes  = { AT_DC, AT_CN };
    String[]         attrNames  = { AT_DC.getNameOrOID(),
                                    AT_CN.getNameOrOID() };
    ByteString[]     attrValues = { AV_DC_ORG, AV_CN };

    RDN rdn = new RDN(attrTypes, attrNames, attrValues);

    assertEquals(rdn.getAttributeType(0), AT_DC);
    assertEquals(rdn.getAttributeType(1), AT_CN);
  }

  /**
   * Test getAttributeValue.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetAttributeValue() throws Exception {
    AttributeType[]  attrTypes  = { AT_DC, AT_CN };
    String[]         attrNames  = { AT_DC.getNameOrOID(),
                                    AT_CN.getNameOrOID() };
    ByteString[]     attrValues = { AV_DC_ORG, AV_CN };

    RDN rdn = new RDN(attrTypes, attrNames, attrValues);

    assertEquals(rdn.getAttributeValue(0), AV_DC_ORG);
    assertEquals(rdn.getAttributeValue(1), AV_CN);
  }

  /**
   * Test getAttributeValue.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetAttributeValueByType() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);

    assertEquals(rdn.getAttributeValue(AT_DC), AV_DC_ORG);
    assertNull(rdn.getAttributeValue(AT_CN));
  }



  /**
   * Test getNumValues.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testGetNumValues() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);
    assertEquals(rdn.getNumValues(), 1);

    rdn.addValue(AT_CN, AT_CN.getNameOrOID(), AV_CN);
    assertEquals(rdn.getNumValues(), 2);
  }



  /**
   * Test hasAttributeType.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testHasAttributeType1() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);

    assertTrue(rdn.hasAttributeType(AT_DC));
    assertTrue(rdn.hasAttributeType("dc"));
    assertTrue(rdn.hasAttributeType(AT_DC.getOID()));
    assertFalse(rdn.hasAttributeType(AT_CN));
    assertFalse(rdn.hasAttributeType("cn"));
  }



  /**
   * Test isMultiValued.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testIsMultiValued() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);
    assertEquals(rdn.getNumValues(), 1);
    assertFalse(rdn.isMultiValued());

    rdn.addValue(AT_CN, AT_CN.getNameOrOID(), AV_CN);
    assertTrue(rdn.isMultiValued());
  }



  /**
   * Tests hasValue.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testHasValue() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);
    assertTrue(rdn.hasValue(AT_DC, AV_DC_ORG));
    assertFalse(rdn.hasValue(AT_CN, AV_CN));

    rdn.addValue(AT_CN, AT_CN.getNameOrOID(), AV_CN);
    assertTrue(rdn.hasValue(AT_DC, AV_DC_ORG));
    assertTrue(rdn.hasValue(AT_CN, AV_CN));
  }



  /**
   * Tests addValue with a duplicate value.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testAddDuplicateValue() throws Exception {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);
    assertFalse(rdn.addValue(AT_DC, AT_DC.getNameOrOID(), AV_DC_ORG));
  }



  /**
   * Test RDN string decoder.
   *
   * @param rawRDN
   *          Raw RDN string representation.
   * @param normRDN
   *          Normalized RDN string representation.
   * @param stringRDN
   *          String representation.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "testRDNs")
  public void testToString(String rawRDN, String normRDN,
      String stringRDN) throws Exception {
    RDN rdn = RDN.decode(rawRDN);
    assertEquals(rdn.toString(), stringRDN);
  }



  /**
   * Tests the duplicate method with a single-valued RDN.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testDuplicateSingle() {
    RDN rdn1 = new RDN(AT_DC, AV_DC_ORG);
    RDN rdn2 = rdn1.duplicate();

    assertNotSame(rdn1, rdn2);
    assertEquals(rdn1, rdn2);
  }



  /**
   * Tests the duplicate method with a multivalued RDN.
   *
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test
  public void testDuplicateMultiValued() {
    AttributeType[]  types  = new AttributeType[] { AT_DC, AT_CN };
    String[]         names  = new String[] { "dc", "cn" };
    ByteString[]     values = new ByteString[] { AV_DC_ORG, AV_CN };

    RDN rdn1 = new RDN(types, names, values);
    RDN rdn2 = rdn1.duplicate();

    assertNotSame(rdn1, rdn2);
    assertEquals(rdn1, rdn2);
  }



  /**
   * RDN equality test data provider.
   *
   * @return The array of test RDN strings.
   */
  @DataProvider(name = "createRDNEqualityData")
  public Object[][] createRDNEqualityData() {
    return new Object[][] {
        { "cn=hello world", "cn=hello world", 0 },
        { "cn=hello world", "CN=hello world", 0 },
        { "cn=hello   world", "cn=hello world", 0 },
        { "  cn =  hello world  ", "cn=hello world", 0 },
        { "cn=hello world\\ ", "cn=hello world", 0 },
        { "cn=HELLO WORLD", "cn=hello world", 0 },
        { "cn=HELLO+sn=WORLD", "sn=world+cn=hello", 0 },
        { "cn=HELLO+sn=WORLD", "cn=hello+sn=nurse", 1 },
        { "cn=HELLO+sn=WORLD", "cn=howdy+sn=yall", -1 },
        { "cn=hello", "cn=hello+sn=world", -1 },
        { "cn=hello+sn=world", "cn=hello", 1 },
        { "cn=hello+sn=world", "cn=hello+description=world", 1 },
        { "cn=hello", "sn=world", -1 },
        { "sn=hello", "cn=world", 1 },
        { "x-test-integer-type=10", "x-test-integer-type=9", 1 },
        { "x-test-integer-type=999", "x-test-integer-type=1000", -1 },
        { "x-test-integer-type=-1", "x-test-integer-type=0", -1 },
        { "x-test-integer-type=0", "x-test-integer-type=-1", 1 },
        { "cn=aaa", "cn=aaaa", -1 }, { "cn=AAA", "cn=aaaa", -1 },
        { "cn=aaa", "cn=AAAA", -1 }, { "cn=aaaa", "cn=aaa", 1 },
        { "cn=AAAA", "cn=aaa", 1 }, { "cn=aaaa", "cn=AAA", 1 },
        { "cn=aaab", "cn=aaaa", 1 }, { "cn=aaaa", "cn=aaab", -1 }
    };
  }



  /**
   * Test RDN equality
   *
   * @param first
   *          First RDN to compare.
   * @param second
   *          Second RDN to compare.
   * @param result
   *          Expected comparison result.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "createRDNEqualityData")
  public void testEquality(String first, String second, int result)
      throws Exception {
    RDN rdn1 = RDN.decode(first);
    RDN rdn2 = RDN.decode(second);

    if (result == 0) {
      assertEquals(rdn1, rdn2,
          "RDN equality for <" + first + "> and <" + second + ">");
    } else {
      assertNotEquals(rdn1, rdn2,
          "RDN equality for <" + first + "> and <" + second + ">");
    }
  }



  /**
   * Test RDN hashCode
   *
   * @param first
   *          First RDN to compare.
   * @param second
   *          Second RDN to compare.
   * @param result
   *          Expected comparison result.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "createRDNEqualityData")
  public void testHashCode(String first, String second, int result)
      throws Exception {
    RDN rdn1 = RDN.decode(first);
    RDN rdn2 = RDN.decode(second);

    int h1 = rdn1.hashCode();
    int h2 = rdn2.hashCode();

    if (result == 0) {
      assertEquals(h1, h2, "Hash codes for <" + first + "> and <" + second
          + "> should be the same.");
    } else {
      assertNotEquals(h1, h2, "Hash codes for <" + first + "> and <" + second
          + "> should be the same.");
    }
  }



  /**
   * Test RDN compareTo
   *
   * @param first
   *          First RDN to compare.
   * @param second
   *          Second RDN to compare.
   * @param result
   *          Expected comparison result.
   * @throws Exception
   *           If the test failed unexpectedly.
   */
  @Test(dataProvider = "createRDNEqualityData")
  public void testCompareTo(String first, String second, int result)
      throws Exception {
    RDN rdn1 = RDN.decode(first);
    RDN rdn2 = RDN.decode(second);

    int rc = rdn1.compareTo(rdn2);

    // Normalize the result.
    if (rc < 0) {
      rc = -1;
    } else if (rc > 0) {
      rc = 1;
    }

    assertEquals(rc, result, "Comparison for <" + first + "> and <" + second + ">.");
  }



  /**
   * Tests the equals method with a null argument.
   */
  @Test
  public void testEqualityNull() {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);

    assertNotEquals(rdn, null);
  }



  /**
   * Tests the equals method with a non-RDN argument.
   */
  @Test
  public void testEqualityNonRDN() {
    RDN rdn = new RDN(AT_DC, AV_DC_ORG);

    assertNotEquals(rdn, "this isn't an RDN");
  }
}

