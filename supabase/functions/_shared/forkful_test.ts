//Pantry matching tests (run: deno test supabase/functions/_shared)

import { pantryMatches, singularize } from "./forkful.ts";

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
