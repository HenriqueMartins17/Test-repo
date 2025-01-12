import Image from 'next/image';
import Link from 'next/link';

import styles from './header.module.scss';

interface LandingHeaderItemProps {
  id: string;
  name: string;
  href: string;
}

const LandingHeaderList: LandingHeaderItemProps[] = [
  {
    id: 'features',
    name: 'Features',
    href: '#features',
  },
  {
    id: 'docs',
    name: 'Docs',
    href: '/docs',
  },
  {
    id: 'pricing',
    name: 'Pricing',
    href: '/pricing',
  },
];

const LandingHeader = () => {
  return (
    <header className={styles.header}>
      <div className={styles.logoSection}>
        <Image src="/file/img/logo.black.svg" alt="Logo" width={200} height={36} />
      </div>
      <div className={styles.navSection}>
        <nav className={styles.nav}>
          {LandingHeaderList.map((item) => {
            return (
              <Link key={item.id} href={item.href} passHref className={styles.navLink}>
                {item.name}
              </Link>
            );
          })}
        </nav>
        <div className={styles.divider} />
        <div>
          <Link href={'/workspace'} passHref className={styles.ctaButton}>
            Get Start Now
          </Link>
        </div>
      </div>
    </header>
  );
};

export default LandingHeader;
