/*
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.data.framework.read.only;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;

/**
 * This is a read only repository that represents the set of AsciiCharacters from 0-256.
 * This repository will be pre-populated at test startup and verified prior to running tests.
 * This interface is required to inherit only from DataRepository in order to satisfy a TCK scenario.
 */
@Repository
public interface AsciiCharacters extends DataRepository<AsciiCharacter, Long>, IdOperations<AsciiCharacter> {

    @Query(" ") // it is valid to have a query with no clauses
    Stream<AsciiCharacter> all(Limit limit, Sort<?>... sort);

    @Query("ORDER BY id ASC")
    Stream<AsciiCharacter> alphabetic(Limit limit);

    @Query("select count(this) where hexadecimal is not null")
    long countByHexadecimalNotNull();

    @Query("select count(this)>0 where thisCharacter = ?1")
    boolean existsByThisCharacter(char ch);

    @Find
    AsciiCharacter find(char thisCharacter);

    @Find
    Optional<AsciiCharacter> find(@By("thisCharacter") char ch,
                                  @By("hexadecimal") String hex);

    @Query("where hexadecimal like '%'||?1||'%' and isControl <> ?2")
    List<AsciiCharacter> findByHexadecimalContainsAndIsControlNot(String substring, boolean isPrintable);

    @Query("where lower(hexadecimal) between lower(?1) and lower(?2) and hexadecimal not in ?3")
    Stream<AsciiCharacter> findByHexadecimalIgnoreCaseBetweenAndHexadecimalNotIn(String minHex,
                                                                                 String maxHex,
                                                                                 List<String> excludeHex,
                                                                                 Order<AsciiCharacter> sorts);
    @Query("where lower(hexadecimal) = lower(?1)")
    AsciiCharacter findByHexadecimalIgnoreCase(String hex);

    @Query("where isControl = true and numericValue between ?1 and ?2")
    AsciiCharacter findByIsControlTrueAndNumericValueBetween(int min, int max);

    @Query("where numericValue = ?1")
    Optional<AsciiCharacter> findByNumericValue(int id);

    @Query("where numericValue between ?1 and ?2")
    Page<AsciiCharacter> findByNumericValueBetween(int min, int max, PageRequest<AsciiCharacter> pagination);

    @Query("where numericValue <= ?1 and numericValue >= ?2")
    List<AsciiCharacter> findByNumericValueLessThanEqualAndNumericValueGreaterThanEqual(int max, int min);

    @Query("where numericValue >= ?1 and right(hexadecimal, length(?2)) = ?2")
    AsciiCharacter[] findByNumericValueGreaterThanEqualAndHexadecimalEndsWith(int minValue, String lastHexDigit, Sort<AsciiCharacter> sort, Limit limit);

    default AsciiCharacter[] findFirst3ByNumericValueGreaterThanEqualAndHexadecimalEndsWith(int minValue, String lastHexDigit, Sort<AsciiCharacter> sort) {
        return findByNumericValueGreaterThanEqualAndHexadecimalEndsWith(minValue , lastHexDigit, sort, Limit.of(3));
    }

    @Query("where left(hexadecimal, length(?1)) = ?1 and isControl = ?2 order by id asc")
    List<AsciiCharacter> findByHexadecimalStartsWithAndIsControlOrderByIdAsc(String firstHexDigit, boolean isControlChar);

    default Optional<AsciiCharacter> findFirstByHexadecimalStartsWithAndIsControlOrderByIdAsc(String firstHexDigit, boolean isControlChar) {
        return findByHexadecimalStartsWithAndIsControlOrderByIdAsc(firstHexDigit, isControlChar).stream().findFirst();
    }

    @Query("select thisCharacter where hexadecimal like '4_'" +
           " and hexadecimal not like '%0'" +
           " and thisCharacter not in ('E', 'G')" +
           " and id not between 72 and 78" +
           " order by id asc")
    Character[] getABCDFO();

    @Query("SELECT hexadecimal WHERE hexadecimal IS NOT NULL AND thisCharacter = ?1")
    Optional<String> hex(char ch);

    @Query("WHERE hexadecimal <> ' ORDER BY isn''t a keyword when inside a literal' AND hexadecimal IN ('4a', '4b', '4c', ?1)")
    Stream<AsciiCharacter> jklOr(String hex);

    default Stream<AsciiCharacter> retrieveAlphaNumericIn(long minId, long maxId) {
        return findByIdBetween(minId, maxId, Sort.asc("id"))
                        .filter(c -> Character.isLetterOrDigit(c.getThisCharacter()));
    }

    @Query("SELECT thisCharacter ORDER BY id DESC")
    Character[] reverseAlphabetic(Limit limit);

    @Save
    List<AsciiCharacter> saveAll(List<AsciiCharacter> characters);

    @Query("SELECT COUNT(THIS) WHERE numericValue <= 97 AND numericValue >= 74")
    long twentyFour();
}
