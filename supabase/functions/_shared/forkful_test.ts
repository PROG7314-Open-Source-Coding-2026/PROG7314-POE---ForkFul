//Matching & sanitiser tests (run: deno test supabase/functions/_shared)

import { isUuid, pantryMatches, sanitizeSearch, singularize } from "./forkful.ts";

function assertEquals(actual: unknown, expected: unknown, label: string) {
  if (actual !== expected) {
    throw new Error(`${label}: expected ${JSON.stringify(expected)} but got ${JSON.stringify(actual)}`);
  }
}

Deno.test("salt does not match salted butter", () => {
  assertEquals(pantryMatches("salt", "salted butter"), false, "salt vs salted butter");
});

Deno.test("salt matches salt and sea salt", () => {
  assertEquals(pantryMatches("salt", "salt"), true, "salt vs salt");
  assertEquals(pantryMatches("Salt", "Sea salt, to taste"), true, "salt vs sea salt");
});

Deno.test("plurals match their singular", () => {
  assertEquals(pantryMatches("tomato", "Tomatoes"), true, "tomato vs tomatoes");
  assertEquals(pantryMatches("Eggs", "2 large free-range eggs"), true, "eggs vs free-range eggs");
  assertEquals(pantryMatches("berries", "berry"), true, "berries vs berry");
  assertEquals(singularize("potatoes"), "potato", "potatoes");
});

Deno.test("multi-word pantry items need every word", () => {
  assertEquals(pantryMatches("olive oil", "extra virgin olive oil"), true, "olive oil");
  assertEquals(pantryMatches("olive oil", "sunflower oil"), false, "olive oil vs sunflower oil");
});

Deno.test("partial words never match", () => {
  assertEquals(pantryMatches("pea", "peanut butter"), false, "pea vs peanut butter");
  assertEquals(pantryMatches("corn", "cornflour"), false, "corn vs cornflour");
  assertEquals(pantryMatches("", "salt"), false, "blank pantry");
});

Deno.test("search text is stripped of filter syntax", () => {
  assertEquals(sanitizeSearch("pasta,title.eq.x"), "pasta title eq x", "comma and dots");
  assertEquals(sanitizeSearch("100% (best)*"), "100 best", "wildcards and brackets");
  assertEquals(sanitizeSearch("  mac   & cheese "), "mac cheese", "whitespace");
  assertEquals(sanitizeSearch("bobotie"), "bobotie", "plain text");
  assertEquals(sanitizeSearch(null), "", "null");
  assertEquals(sanitizeSearch("a".repeat(100)).length, 60, "length cap");
});

Deno.test("uuid validation", () => {
  assertEquals(isUuid("0b6e7c1a-3f7d-4a5e-9c1b-2d4e6f8a0b1c"), true, "valid uuid");
  assertEquals(isUuid("1 or 1=1"), false, "injection text");
  assertEquals(isUuid(null), false, "null");
});
