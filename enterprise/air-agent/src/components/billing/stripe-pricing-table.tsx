import React, { useEffect } from 'react';

export default function StripePricingTable({ customerEmail }: { customerEmail?: string }) {
  useEffect(() => {
    const script = document.createElement('script');
    script.src = 'https://js.stripe.com/v3/pricing-table.js';
    script.async = true;
    document.body.appendChild(script);
    return () => {
      document.body.removeChild(script);
    };
  }, []);

  return React.createElement('stripe-pricing-table', {
    'pricing-table-id': 'prctbl_1O2nPHLGyXnYfGi6qoCdx6sX',
    'publishable-key': 'pk_live_51O2XWcLGyXnYfGi6bAQDP5NWCGoJgkFXqYTVSuMa24g6c50VEYGA6W9mJg29VfuG675LEzYceRRUgdIaYdhcs6C600ewSVYBKv',
    'customer-email': customerEmail,
  });
}
