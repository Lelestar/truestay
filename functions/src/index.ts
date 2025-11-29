import {setGlobalOptions} from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
setGlobalOptions({maxInstances: 2});

// Functions
export {onRentalWrite} from "./availability";
export {onRentalAccepted} from "./inventories";
export {onReviewWrite} from "./reviews";
export {generateInventoryPdf, onInventorySign} from "./inventory";

