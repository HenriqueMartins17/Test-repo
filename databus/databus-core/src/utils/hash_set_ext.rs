use std::collections::HashSet;

pub fn is_same_set<T, S>(set1: &HashSet<T, S>, set2: &HashSet<T, S>) -> bool
  where
    T: Eq + std::hash::Hash,
    S: std::hash::BuildHasher,
{
  if set1.len() != set2.len() {
    return false;
  }

  for item in set1 {
    if !set2.contains(item) {
      return false;
    }
  }

  true
}

pub fn has_intersect<T, S>(set1: &HashSet<T, S>, set2: &HashSet<T, S>) -> bool
where
  T: Eq + std::hash::Hash,
  S: std::hash::BuildHasher,
{
  set1.intersection(set2).count() > 0
}
