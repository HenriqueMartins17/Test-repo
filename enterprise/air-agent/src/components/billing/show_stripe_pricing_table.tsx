import { Modal } from 'antd';
import { createRoot } from 'react-dom/client';
import StripePricingTable from './stripe-pricing-table';

export const showStripePricingTable = (email: string) => {
  const div = document.createElement('div');
  div.setAttribute('class', 'funcAlert');
  document.body.appendChild(div);
  const root = createRoot(div);

  function destroy() {
    root.unmount();
    div.parentElement!.removeChild(div);
  }

  return root.render(
    <Modal title="Upgrade Plan" open onOk={destroy} onCancel={destroy} footer={null} width={1600} zIndex={1100}>
      <StripePricingTable customerEmail={email} />
    </Modal>,
  );
};
