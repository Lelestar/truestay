import {setGlobalOptions} from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
setGlobalOptions({maxInstances: 2});

// Functions
export {onRentalWrite} from "./availability";
export {onReviewWrite} from "./reviews";

