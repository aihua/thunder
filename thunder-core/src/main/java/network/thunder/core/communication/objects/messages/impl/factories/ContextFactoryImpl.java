package network.thunder.core.communication.objects.messages.impl.factories;

import network.thunder.core.communication.objects.messages.impl.*;
import network.thunder.core.communication.objects.messages.interfaces.factories.*;
import network.thunder.core.communication.objects.messages.interfaces.helper.*;
import network.thunder.core.communication.processor.implementations.AuthenticationProcessorImpl;
import network.thunder.core.communication.processor.implementations.EncryptionProcessorImpl;
import network.thunder.core.communication.processor.implementations.LNEstablishProcessorImpl;
import network.thunder.core.communication.processor.implementations.PeerSeedProcessorImpl;
import network.thunder.core.communication.processor.implementations.gossip.BroadcastHelper;
import network.thunder.core.communication.processor.implementations.gossip.GossipProcessorImpl;
import network.thunder.core.communication.processor.implementations.gossip.GossipSubject;
import network.thunder.core.communication.processor.implementations.gossip.GossipSubjectImpl;
import network.thunder.core.communication.processor.implementations.lnpayment.LNPaymentLogicImpl;
import network.thunder.core.communication.processor.implementations.lnpayment.LNPaymentProcessorImpl;
import network.thunder.core.communication.processor.implementations.sync.SyncProcessorImpl;
import network.thunder.core.communication.processor.implementations.sync.SynchronizationHelper;
import network.thunder.core.communication.processor.interfaces.*;
import network.thunder.core.communication.processor.interfaces.lnpayment.LNPaymentLogic;
import network.thunder.core.communication.processor.interfaces.lnpayment.LNPaymentProcessor;
import network.thunder.core.database.DBHandler;
import network.thunder.core.mesh.Node;
import org.bitcoinj.core.Wallet;

/**
 * Created by matsjerratsch on 18/01/2016.
 */
public class ContextFactoryImpl implements ContextFactory {
    DBHandler dbHandler;
    LNEventHelper eventHelper;

    SynchronizationHelper syncHelper;
    GossipSubject gossipSubject;
    BroadcastHelper broadcastHelper;
    WalletHelper walletHelper;

    LNPaymentHelper paymentHelper;

    LNOnionHelper onionHelper;

    public ContextFactoryImpl (DBHandler dbHandler, Wallet wallet, LNEventHelper eventHelper) {
        this.dbHandler = dbHandler;
        this.eventHelper = eventHelper;
        this.walletHelper = new WalletHelperImpl(wallet);

        GossipSubjectImpl gossipSubject = new GossipSubjectImpl(dbHandler);
        this.gossipSubject = gossipSubject;
        this.broadcastHelper = gossipSubject;

        this.syncHelper = new SynchronizationHelper(dbHandler);

        this.onionHelper = new LNOnionHelperImpl();

        this.paymentHelper = new LNPaymentHelperImpl(onionHelper, dbHandler);
    }

    @Override
    public MessageSerializer getMessageSerializer () {
        return new MessageSerializerImpl();
    }

    @Override
    public MessageEncrypter getMessageEncrypter () {
        return new MessageEncrypterImpl(getMessageSerializer());
    }

    @Override
    public EncryptionProcessor getEncryptionProcessor (Node node) {
        MessageEncrypter messageEncrypter = getMessageEncrypter();
        EncryptionMessageFactory encryptionMessageFactory = new EncryptionMessageFactoryImpl();
        return new EncryptionProcessorImpl(encryptionMessageFactory, messageEncrypter, node);
    }

    @Override
    public AuthenticationProcessor getAuthenticationProcessor (Node node) {
        return new AuthenticationProcessorImpl(new AuthenticationMessageFactoryImpl(), eventHelper, node);
    }

    @Override
    public PeerSeedProcessor getPeerSeedProcessor (Node node) {
        return new PeerSeedProcessorImpl(new PeerSeedMessageFactoryImpl(), dbHandler, eventHelper, node);
    }

    @Override
    public SyncProcessor getSyncProcessor (Node node) {
        SyncMessageFactory messageFactory = new SyncMessageFactoryImpl();
        return new SyncProcessorImpl(messageFactory, node, syncHelper);
    }

    @Override
    public GossipProcessor getGossipProcessor (Node node) {
        GossipMessageFactory messageFactory = new GossipMessageFactoryImpl();
        return new GossipProcessorImpl(messageFactory, gossipSubject, dbHandler, node);
    }

    @Override
    public LNEstablishProcessor getLNEstablishProcessor (Node node) {
        LNEstablishMessageFactory messageFactory = new LNEstablishMessageFactoryImpl();
        return new LNEstablishProcessorImpl(walletHelper, messageFactory, broadcastHelper, eventHelper, node);
    }

    @Override
    public LNPaymentProcessor getLNPaymentProcessor (Node node) {
        LNPaymentMessageFactory messageFactory = new LNPaymentMessageFactoryImpl(dbHandler);
        LNPaymentLogic paymentLogic = new LNPaymentLogicImpl(dbHandler);
        return new LNPaymentProcessorImpl(messageFactory, paymentLogic, dbHandler, paymentHelper, node);
    }

}
