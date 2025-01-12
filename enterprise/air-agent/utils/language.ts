export enum LanguageName {
  English = 'en-US',
  Chinese = 'zh-CN',
}

export default function updateLanguage(lang: LanguageName) {
  localStorage.setItem('client-lang', lang);
  window.location.reload();
}
