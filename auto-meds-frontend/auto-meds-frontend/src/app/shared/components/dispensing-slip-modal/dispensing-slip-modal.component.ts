import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { DispensingSlip } from '../../../core/models/compliance-and-payment.model';
import { QrCodeGenerator } from '../../../core/utils/qr-code.util';

@Component({
  selector: 'app-dispensing-slip-modal',
  templateUrl: './dispensing-slip-modal.component.html',
  styleUrls: ['./dispensing-slip-modal.component.css']
})
export class DispensingSlipModalComponent implements OnChanges {
  @Input() slip: DispensingSlip | null = null;
  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();

  qrCodeDataUrl: string = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['slip'] && this.slip) {
      this.generateQr();
    }
  }

  generateQr(): void {
    if (!this.slip) return;
    const payload = this.slip.qrPayload || `AUTOMEDS:SLIP:${this.slip.dispensingSlipCode}:ORDER:${this.slip.orderId}`;
    this.qrCodeDataUrl = QrCodeGenerator.generateDataUrl(payload, 160);
  }

  onClose(): void {
    this.close.emit();
  }

  printSlip(): void {
    window.print();
  }
}
